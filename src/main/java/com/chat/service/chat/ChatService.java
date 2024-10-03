package com.chat.service.chat;


import static org.apache.logging.log4j.util.Strings.isEmpty;

import com.chat.domain.Chat;
import com.chat.domain.channel.Channel;
import com.chat.domain.channel.ChannelUserRelation;
import com.chat.domain.server.Server;
import com.chat.domain.user.User;
import com.chat.dto.MessageDto;
import com.chat.dto.MessageDto.MessageType;
import com.chat.dto.chat.ChatInfoDto;
import com.chat.dto.chat.ChatListResponseDto;
import com.chat.dto.chat.ChatSearchRequestDto;
import com.chat.dto.chat.ChatSearchResponseDto;
import com.chat.dto.chat.SendMessageResponseDto;
import com.chat.exception.ChannelException;
import com.chat.exception.ChatException;
import com.chat.exception.ServerException;
import com.chat.exception.UserException;
import com.chat.repository.channel.ChannelRepository;
import com.chat.repository.channel.ChannelUserRelationRepository;
import com.chat.repository.chat.ChatRepository;
import com.chat.repository.server.ServerRepository;
import com.chat.repository.server.ServerUserRelationRepository;
import com.chat.repository.user.UserRepository;
import com.chat.service.user.CustomUserDetailsService;
import com.chat.util.websocket.StompAfterCommitSynchronization;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatService {

  private final CustomUserDetailsService customUserDetailsService;
  private final UserRepository userRepository;
  private final ServerUserRelationRepository serverUserRelationRepository;
  private final ChannelRepository channelRepository;
  private final ChatRepository chatRepository;
  private final ChannelUserRelationRepository channelUserRelationRepository;
  private final ServerRepository serverRepository;

  private static final String USER_UNREGISTERED = "USER:USER_UNREGISTERED";
  private static final String SERVER_NOT_FOUND = "SERVER:SERVER_NOT_FOUND";
  private static final String CHAT_NOT_FOUND = "CHAT:CHAT_NOT_FOUND";
  private static final String PAGE_INVALID = "VALID:PAGE_INVALID";
  private static final String CHANNEL_NOT_FOUND = "CHANNEL:CHANNEL_NOT_FOUND";
  private static final String CHANNEL_NOT_PARTICIPATED = "CHANNEL:CHANNEL_NOT_PARTICIPATED";

  private final SimpMessagingTemplate messagingTemplate;
  private static final String SUB_SERVER = "/sub/server/";
  private static final String SUB_CHANNEL = "/sub/channel/";


  @Transactional
  public SendMessageResponseDto sendMessage(MessageDto messageDto) {
    String email = customUserDetailsService.getEmailByUserDetails();

    Long serverId = messageDto.getServerId();
    Long channelId = messageDto.getChannelId();
    String message = messageDto.getMessage();

    // 해당 서버,채널 참여자인지 확인
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));
    Server server = serverUserRelationRepository.fetchServerByUserAndServerId(user, serverId)
        .orElseThrow(() -> new ServerException(SERVER_NOT_FOUND));
    Channel channel = channelRepository.findByIdAndLogicDeleteFalseAndServerId(channelId, serverId)
        .orElseThrow(() -> new ChannelException(CHANNEL_NOT_FOUND));
    ChannelUserRelation channelUserRelation = channelUserRelationRepository.findByChannelAndUser(
            channel, user)
        .orElseThrow(() -> new ChannelException(CHANNEL_NOT_PARTICIPATED));

    LocalDateTime createTime = LocalDateTime.now();
    // 메세지 저장
    Chat chat = Chat.builder()
        .message(message)
        .server(server)
        .channel(channel)
        .user(user)
        .logicDelete(false)
        .createTime(createTime)
        .updateTime(createTime)
        .build();
    chatRepository.save(chat);

    // record server, channel lastMessage
    Long chatId = chat.fetchChatIdForUpdateLastMessage();
    channel.updateLastMessageId(chatId);
    channelUserRelation.updateLastReadMessageId(chatId);
    channelRepository.save(channel);
    channelUserRelationRepository.save(channelUserRelation);

    // stomp pub
    String channelUrl = SUB_CHANNEL + serverId + "/" + channelId;
    String avatar = user.fetchAvatarForSendMessageResponse();
    MessageDto newMessageDto = chat.buildMessageDtoForSendMessageResponse(messageDto, avatar);
    TransactionSynchronizationManager.registerSynchronization(
        new StompAfterCommitSynchronization(messagingTemplate, channelUrl, newMessageDto)
    );

    // return
    Long id = chat.fetchChatIdForSendMessageResponse();
    return SendMessageResponseDto.builder()
        .serverId(serverId)
        .channelId(channelId)
        .id(id)
        .createTime(createTime)
        .avatar(avatar)
        .build();
  }

  @Transactional
  public void updateMessage(MessageDto messageDto) {
    Long serverId = messageDto.getServerId();
    Long channelId = messageDto.getChannelId();
    Long chatId = messageDto.getChatId();
    String message = messageDto.getMessage();

    String email = customUserDetailsService.getEmailByUserDetails();

    // 해당 서버,채널 참여자인지 확인
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));
    if (serverUserRelationRepository.fetchServerByUserAndServerId(user, serverId).isEmpty()) {
      throw new ServerException(SERVER_NOT_FOUND);
    }
    if (channelUserRelationRepository.findByChannelIdAndUser(channelId, user).isEmpty()) {
      throw new ChannelException(CHANNEL_NOT_PARTICIPATED);
    }

    Chat chat = chatRepository.findByIdAndUserAndLogicDeleteFalse(chatId, user)
        .orElseThrow(() -> new ChatException(CHAT_NOT_FOUND));

    LocalDateTime updateTime = LocalDateTime.now();
    chat.updateMessage(messageDto, updateTime);
    chatRepository.save(chat);

    // stomp pub
    String channelUrl = SUB_CHANNEL + serverId + "/" + channelId;
    MessageDto newMessageDto = MessageDto.builder()
        .messageType(MessageType.CHAT_UPDATE)
        .serverId(serverId)
        .channelId(channelId)
        .chatId(chatId)
        .username(messageDto.getUsername())
        .message(message)
        .updateTime(updateTime)
        .build();
    messagingTemplate.convertAndSend(channelUrl, newMessageDto);
  }

  public ChatListResponseDto chatList(Long serverId, Long channelId) {
    String email = customUserDetailsService.getEmailByUserDetails();

    // 해당 서버,채널 참여자인지 확인
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));
    if (serverUserRelationRepository.fetchServerByUserAndServerId(user, serverId).isEmpty()) {
      throw new ServerException(SERVER_NOT_FOUND);
    }
    if (channelUserRelationRepository.findByChannelIdAndUser(channelId, user).isEmpty()) {
      throw new ChannelException(CHANNEL_NOT_PARTICIPATED);
    }

    // 최근 50개 fetch
    List<ChatInfoDto> chatInfoDtoList = chatRepository.fetchChatInfoDtoListByChannelId(channelId);

    return ChatListResponseDto.builder()
        .chatList(chatInfoDtoList)
        .build();
  }

  public ChatListResponseDto chatListPrevious(Long serverId, Long channelId, Long chatId) {
    String email = customUserDetailsService.getEmailByUserDetails();

    // 해당 서버,채널 참여자인지 확인
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));
    if (serverUserRelationRepository.fetchServerByUserAndServerId(user, serverId).isEmpty()) {
      throw new ServerException(SERVER_NOT_FOUND);
    }
    if (channelUserRelationRepository.findByChannelIdAndUser(channelId, user).isEmpty()) {
      throw new ChannelException(CHANNEL_NOT_PARTICIPATED);
    }

    // 주어진 chatId의 앞50개 fetch
    List<ChatInfoDto> chatInfoDtoList = chatRepository
        .fetchChatInfoDtoListByChannelIdAndChatId(channelId, chatId);

    return ChatListResponseDto.builder()
        .chatList(chatInfoDtoList)
        .build();
  }

  @Transactional
  public void delete(Long serverId, Long channelId, Long chatId) {
    String email = customUserDetailsService.getEmailByUserDetails();

    // 해당 서버,채널 참여자인지 확인
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));
    if (serverUserRelationRepository.fetchServerByUserAndServerId(user, serverId).isEmpty()) {
      throw new ServerException(SERVER_NOT_FOUND);
    }
    if (channelUserRelationRepository.findByChannelIdAndUser(channelId, user).isEmpty()) {
      throw new ChannelException(CHANNEL_NOT_PARTICIPATED);
    }

    Chat chat = chatRepository.findByIdAndUserAndLogicDeleteFalse(chatId, user)
        .orElseThrow(() -> new ChatException(CHAT_NOT_FOUND));

    chat.logicDelete();
    chatRepository.save(chat);

    // stomp pub
    String channelUrl = SUB_CHANNEL + serverId + "/" + channelId;
    MessageDto newMessageDto = MessageDto.builder()
        .messageType(MessageType.CHAT_DELETE)
        .serverId(serverId)
        .channelId(channelId)
        .chatId(chatId)
        .build();
    messagingTemplate.convertAndSend(channelUrl, newMessageDto);
  }

  // todo 수정필요
  public ChatSearchResponseDto search(Long serverId, ChatSearchRequestDto requestDto, int page,
      int size) {
    String keyword = requestDto.getKeyword();
    String username = requestDto.getUsername();
    String message = requestDto.getMessage();

    // 서버유저인지 검증
    String email = customUserDetailsService.getEmailByUserDetails();

    // 해당 서버,채널 참여자인지 확인
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));
    // todo role check
    // 현재는 참여자확인만 이루어짐
    if (serverUserRelationRepository.fetchServerByUserAndServerId(user, serverId).isEmpty()) {
      throw new ServerException(SERVER_NOT_FOUND);
    }

    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Direction.DESC, "id"));
    Page<ChatInfoDto> chatPage = null;

    if (!isEmpty(keyword)) {
      // 검색 기본쿼리
      chatPage = chatRepository.searchChatInfoDtoListDefault(serverId, keyword, pageable);
    }

    if (isEmpty(keyword) && !isEmpty(username) && !isEmpty(message)) {
      // 둘을 검색하는 쿼리
      chatPage = chatRepository.searchChatInfoDtoListByUsernameAndMessage(serverId, username,
          message, pageable);
    }

    if (isEmpty(keyword) && !isEmpty(username) && isEmpty(message)) {
      // 유저명으로만 검색
      chatPage = chatRepository.searchChatInfoDtoListByUsername(serverId, username, pageable);
    }

    if (isEmpty(keyword) && isEmpty(username) && !isEmpty(message)) {
      // 내용만으로 검색
      chatPage = chatRepository.searchChatInfoDtoListByMessage(serverId, message, pageable);
    }

    assert chatPage != null;
    validatePage(page, chatPage);

    int currentPage = chatPage.getNumber() + 1;
    Integer next = (!chatPage.hasNext()) ? null : currentPage + 1;
    Integer previous = (!chatPage.hasPrevious()) ? null : currentPage - 1;
    List<ChatInfoDto> chatInfoDtoList = chatPage.stream().toList();

    return ChatSearchResponseDto.builder()
        .next(next)
        .previous(previous)
        .total(chatPage.getTotalPages())
        .page(page)
        .size(size)
        .chatInfoDtoList(chatInfoDtoList)
        .build();
  }


  private void validatePage(Integer page, Page<?> pageList) {
    if ((page - 1 > pageList.getTotalPages() && !pageList.isEmpty())
        || (page > 1 && pageList.isEmpty())) {
      // 초과 에러 리턴
      throw new ServerException(PAGE_INVALID);
    }
  }
}
