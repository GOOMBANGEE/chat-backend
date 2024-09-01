package com.chat.service.chat;


import static org.apache.logging.log4j.util.Strings.isEmpty;

import com.chat.domain.Chat;
import com.chat.domain.server.Server;
import com.chat.domain.user.User;
import com.chat.dto.MessageDto;
import com.chat.dto.MessageDto.MessageType;
import com.chat.dto.chat.ChatInfoDto;
import com.chat.dto.chat.ChatListResponseDto;
import com.chat.dto.chat.ChatSearchRequestDto;
import com.chat.dto.chat.ChatSearchResponseDto;
import com.chat.dto.chat.SendMessageResponseDto;
import com.chat.exception.ServerException;
import com.chat.repository.chat.ChatRepository;
import com.chat.repository.server.ServerUserRelationRepository;
import com.chat.repository.user.UserRepository;
import com.chat.service.user.CustomUserDetailsService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatService {

  private final CustomUserDetailsService customUserDetailsService;
  private final UserRepository userRepository;
  private final ServerUserRelationRepository serverUserRelationRepository;
  private final ChatRepository chatRepository;

  private static final String USER_UNREGISTERED = "SERVER:USER_UNREGISTERED";
  private static final String SERVER_NOT_FOUND = "SERVER:SERVER_NOT_FOUND";
  private static final String CHAT_NOT_FOUND = "SERVER:CHAT_NOT_FOUND";
  private static final String PAGE_INVALID = "VALID:PAGE_INVALID";


  private final SimpMessagingTemplate messagingTemplate;
  private static final String SUB_SERVER = "/sub/server/";

  @Transactional
  public SendMessageResponseDto sendMessage(MessageDto messageDto) {
    String email = customUserDetailsService.getEmailByUserDetails();

    Long serverId = messageDto.getServerId();
    String message = messageDto.getMessage();

    // 해당 서버 참여자인지 확인
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new ServerException(USER_UNREGISTERED));

    // todo role check
    // 현재는 참여자확인만 이루어짐
    Server server = serverUserRelationRepository.findServerByUserAndServerId(user, serverId)
        .orElseThrow(() -> new ServerException(SERVER_NOT_FOUND));

    LocalDateTime createTime = LocalDateTime.now();
    // 메세지 저장
    Chat chat = Chat.builder()
        .message(message)
        .server(server)
        .user(user)
        .logicDelete(false)
        .createTime(createTime)
        .updateTime(createTime)
        .build();
    chatRepository.save(chat);

    // stomp pub
    String serverUrl = SUB_SERVER + serverId;
    MessageDto newMessageDto = chat.buildMessageDtoForSendMessageResponse(messageDto);
    messagingTemplate.convertAndSend(serverUrl, newMessageDto);

    // return
    Long id = chat.fetchChatIdForSendMessageResponse();
    return SendMessageResponseDto.builder()
        .serverId(serverId)
        .id(id)
        .createTime(createTime)
        .build();
  }

  @Transactional
  public void updateMessage(MessageDto messageDto) {
    Long serverId = messageDto.getServerId();
    Long chatId = messageDto.getChatId();
    String message = messageDto.getMessage();

    String email = customUserDetailsService.getEmailByUserDetails();

    // 해당 서버 참여자인지 확인
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new ServerException(USER_UNREGISTERED));

    // todo role check
    // 현재는 참여자확인만 이루어짐
    if (serverUserRelationRepository.findServerByUserAndServerId(user, serverId).isEmpty()) {
      throw new ServerException(SERVER_NOT_FOUND);
    }

    Chat chat = chatRepository.findByIdAndUserAndLogicDeleteFalse(chatId, user)
        .orElseThrow(() -> new ServerException(CHAT_NOT_FOUND));

    LocalDateTime updateTime = LocalDateTime.now();
    chat.updateMessage(messageDto, updateTime);
    chatRepository.save(chat);

    // stomp pub
    String serverUrl = SUB_SERVER + serverId;
    MessageDto newMessageDto = MessageDto.builder()
        .messageType(MessageType.UPDATE_CHAT)
        .serverId(serverId)
        .chatId(chatId)
        .username(messageDto.getUsername())
        .message(message)
        .updateTime(updateTime)
        .build();
    messagingTemplate.convertAndSend(serverUrl, newMessageDto);
  }

  public ChatListResponseDto list(Long serverId) {
    String email = customUserDetailsService.getEmailByUserDetails();

    // 해당 서버 참여자인지 확인
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new ServerException(USER_UNREGISTERED));

    // todo role check
    // 현재는 참여자확인만 이루어짐
    if (serverUserRelationRepository.findServerByUserAndServerId(user, serverId).isEmpty()) {
      throw new ServerException(SERVER_NOT_FOUND);
    }

    // 최근 50개 fetch
    List<ChatInfoDto> chatInfoDtoList = chatRepository.fetchChatInfoDtoListByServerId(serverId);

    return ChatListResponseDto.builder()
        .chatList(chatInfoDtoList)
        .build();
  }

  @Transactional
  public void delete(Long serverId, Long chatId) {
    String email = customUserDetailsService.getEmailByUserDetails();

    // 해당 서버 참여자인지 확인
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new ServerException(USER_UNREGISTERED));

    // todo role check
    // 현재는 참여자확인만 이루어짐
    if (serverUserRelationRepository.findServerByUserAndServerId(user, serverId).isEmpty()) {
      throw new ServerException(SERVER_NOT_FOUND);
    }

    Chat chat = chatRepository.findByIdAndUserAndLogicDeleteFalse(chatId, user)
        .orElseThrow(() -> new ServerException(CHAT_NOT_FOUND));

    chat.logicDelete();
    chatRepository.save(chat);

    // stomp pub
    String serverUrl = SUB_SERVER + serverId;
    MessageDto newMessageDto = MessageDto.builder()
        .messageType(MessageType.DELETE_CHAT)
        .serverId(serverId)
        .chatId(chatId)
        .build();
    messagingTemplate.convertAndSend(serverUrl, newMessageDto);
  }

  public ChatSearchResponseDto search(Long serverId, ChatSearchRequestDto requestDto, int page,
      int size) {
//    List<ChatInfoDto>
    String keyword = requestDto.getKeyword();
    String username = requestDto.getUsername();
    String message = requestDto.getMessage();

    // 서버유저인지 검증
    String email = customUserDetailsService.getEmailByUserDetails();

    // 해당 서버 참여자인지 확인
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new ServerException(USER_UNREGISTERED));

    // todo role check
    // 현재는 참여자확인만 이루어짐
    if (serverUserRelationRepository.findServerByUserAndServerId(user, serverId).isEmpty()) {
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
