package com.chat.service.chat;


import static org.apache.logging.log4j.util.Strings.isEmpty;

import com.chat.domain.channel.Channel;
import com.chat.domain.channel.ChannelUserRelation;
import com.chat.domain.chat.Chat;
import com.chat.domain.server.Server;
import com.chat.domain.user.Notification;
import com.chat.domain.user.User;
import com.chat.dto.MessageDto;
import com.chat.dto.MessageDto.MessageType;
import com.chat.dto.chat.ChatInfoDto;
import com.chat.dto.chat.ChatListResponseDto;
import com.chat.dto.chat.ChatReferenceInfoForSendMessageResponse;
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
import com.chat.repository.server.ServerUserRelationRepository;
import com.chat.repository.user.NotificationRepository;
import com.chat.repository.user.UserRepository;
import com.chat.service.user.CustomUserDetailsService;
import com.chat.util.UUIDGenerator;
import com.chat.util.websocket.StompAfterCommitSynchronization;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
  private final NotificationRepository notificationRepository;
  private final UUIDGenerator uuidGenerator;

  private static final String USER_UNREGISTERED = "USER:USER_UNREGISTERED";
  private static final String SERVER_NOT_FOUND = "SERVER:SERVER_NOT_FOUND";
  private static final String CHAT_NOT_FOUND = "CHAT:CHAT_NOT_FOUND";
  private static final String PAGE_INVALID = "VALID:PAGE_INVALID";
  private static final String CHANNEL_NOT_FOUND = "CHANNEL:CHANNEL_NOT_FOUND";
  private static final String CHANNEL_NOT_PARTICIPATED = "CHANNEL:CHANNEL_NOT_PARTICIPATED";
  private static final String UNSUPPORTED_FILE_TYPE = "CHAT:UNSUPPORTED_FILE_TYPE";

  private final SimpMessagingTemplate messagingTemplate;
  private static final String SUB_CHANNEL = "/sub/channel/";
  private static final String EXTENSION = "extension";
  private static final String PATH = "path";

  @Value("${server.file-path.chat.audio}")
  private String filePathChatAudio;
  @Value("${server.file-path.chat.image}")
  private String filePathChatImage;
  @Value("${server.file-path.chat.text}")
  private String filePathChatText;
  @Value("${server.file-path.chat.video}")
  private String filePathChatVideo;
  @Value("${server.file-path.chat.application.json}")
  private String filePathChatJson;
  @Value("${server.file-path.chat.application.pdf}")
  private String filePathChatPdf;
  @Value("${server.file-path.chat.application.zip}")
  private String filePathChatApplicationZip;
  @Value("${server.time-zone}")
  private String timeZone;

  @Transactional
  public SendMessageResponseDto sendMessage(MessageDto messageDto) throws IOException {
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
    ChannelUserRelation channelUserRelation = channelUserRelationRepository
        .findByChannelAndUser(channel, user)
        .orElseThrow(() -> new ChannelException(CHANNEL_NOT_PARTICIPATED));

    // attachment logic
    String attachment = messageDto.getAttachment();
    String mimeType = null;
    String filePath = null;
    if (attachment != null) {
      String[] base64 = attachment.split(",");
      // mimeType -> data:image/jpeg,png,gif,bmp,webp, video/mp4,mpeg,ogg,
      // audio/mpeg,wav,ogg,mp4, text/plain, application/json,pdf,zip
      String metadata = base64[0];
      String base64Data = base64[1];
      mimeType = metadata.split(":")[1].split(";")[0];  // image/png

      // 파일 확장자, 폴더 결정
      Map<String, String> attachmentInfo = getFileInfoFromMimeType(mimeType);
      if (attachmentInfo.get(EXTENSION) == null) {
        throw new ChatException(UNSUPPORTED_FILE_TYPE);
      }
      String extension = attachmentInfo.get(EXTENSION);
      String path = attachmentInfo.get(PATH);

      // base64 데이터를 바이트 배열로 디코딩
      byte[] decode = Base64.getDecoder().decode(base64Data);

      // 현재 시간 millisecond
      ZoneId zoneid = ZoneId.of(timeZone);
      long epochMilli = LocalDateTime.now().atZone(zoneid).toInstant().toEpochMilli();

      String fileName = uuidGenerator.generateUUID() + "_" + epochMilli + "." + extension;
      filePath = path + fileName;

      // 파일 저장
      Files.write(Paths.get(filePath), decode);
    }

    LocalDateTime createTime = LocalDateTime.now();
    // 메세지 저장
    Chat chat = Chat.builder()
        .server(server)
        .channel(channel)
        .user(user)
        .message(message)
        .attachmentType(mimeType)
        .attachment(filePath)
        .createTime(createTime)
        .updateTime(createTime)
        .build();

    // mention logic
    List<Long> mentionedUserIdList = new ArrayList<>();
    // message 안에서 <@userId> 부분 모두 찾기
    String regex = "<@(\\d+)>";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(message);

    // 매칭되는 모든 userId 추출
    while (matcher.find()) {
      // matcher.group(1)은 숫자만 반환하므로 Long으로 변환하여 리스트에 추가
      Long userId = Long.parseLong(matcher.group(1));
      mentionedUserIdList.add(userId);
    }

    // 멘션받는 대상이 채널에 속해있는지 확인
    if (!mentionedUserIdList.isEmpty()) {
      List<User> mentionedUserList = userRepository.findByIdInAndLogicDeleteFalse(
          mentionedUserIdList);
      List<ChannelUserRelation> channelUserRelationList = channelUserRelationRepository
          .findByChannelAndUserIn(channel, mentionedUserList);
      if (channelUserRelationList.size() != mentionedUserList.size()) {
        throw new ChannelException(CHANNEL_NOT_PARTICIPATED);
      }

      // mention notification
      List<Notification> notificationList = new ArrayList<>();
      mentionedUserList.forEach(mentionedUser -> {
        Notification notification = Notification.builder()
            .server(server)
            .channel(channel)
            .chat(chat)
            .user(user)
            .mentionedUser(mentionedUser)
            .build();
        notificationList.add(notification);
      });
      notificationRepository.saveAll(notificationList);
    }

    // reply
    Long chatReferenceId = messageDto.getChatReference();
    ChatInfoDto chatRefInfoDto = null;
    if (chatReferenceId != null) {
      ChatReferenceInfoForSendMessageResponse chatReferenceInfo = chatRepository
          .fetchChatReferenceInfoForSendMessageResponseByChatIdAndChannel(chatReferenceId, channel);

      User mentionedUser = chatReferenceInfo.getUser();
      if (messageDto.isChatReferenceNotification()) {
        Notification notification = Notification.builder()
            .server(server)
            .channel(channel)
            .chat(chat)
            .user(user)
            .mentionedUser(mentionedUser)
            .build();
        notificationRepository.save(notification);
      }

      Chat chatReference = chatReferenceInfo.getChat();
      chat.updateChatReference(chatReference);
      chatRefInfoDto = ChatInfoDto.builder()
          .id(chatReferenceInfo.getId())
          .username(chatReferenceInfo.getUsername())
          .avatarImageSmall(chatReferenceInfo.getAvatarImageSmall())
          .message(chatReferenceInfo.getMessage())
          .attachmentType(chatReferenceInfo.getAttachmentType())
          .build();
    }

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
    MessageDto newMessageDto;
    if (chatReferenceId != null) {
      newMessageDto = chat
          .buildMessageDtoForSendMessageResponse(messageDto, avatar, chatRefInfoDto);
    } else {
      newMessageDto = chat
          .buildMessageDtoForSendMessageResponse(messageDto, avatar, null);
    }
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

  private Map<String, String> getFileInfoFromMimeType(String mimeType) {
    // MIME 타입과 관련 정보 매핑
    Map<String, String> mimeTypeMapping = Map.ofEntries(
        Map.entry("audio/mpeg", "mp3:" + filePathChatAudio),
        Map.entry("audio/wav", "wav" + filePathChatAudio),
        Map.entry("audio/mp4", "aac" + filePathChatAudio),
        Map.entry("image/jpeg", "jpg:" + filePathChatImage),
        Map.entry("image/png", "png:" + filePathChatImage),
        Map.entry("image/gif", "gif:" + filePathChatImage),
        Map.entry("image/bmp", "bmp" + filePathChatImage),
        Map.entry("image/webp", "webp" + filePathChatImage),
        Map.entry("text/plain", "txt:" + filePathChatText),
        Map.entry("video/mp4", "mp4" + filePathChatVideo),
        Map.entry("video/mpeg", "mpeg" + filePathChatVideo),
        Map.entry("video/ogg", "ogg" + filePathChatVideo),
        Map.entry("application/json", "json" + filePathChatJson),
        Map.entry("application/pdf", "pdf" + filePathChatPdf),
        Map.entry("application/zip", "zip:" + filePathChatApplicationZip)
    );

    // MIME 타입에 따른 파일 정보 반환
    String fileInfo = mimeTypeMapping.get(mimeType);
    if (fileInfo == null) {
      return Collections.emptyMap(); // 처리되지 않은 MIME 타입
    }

    // 파일 정보를 분리하여 반환
    String[] parts = fileInfo.split(":");
    Map<String, String> attachmentInfo = new HashMap<>();
    attachmentInfo.put(EXTENSION, parts[0]);
    attachmentInfo.put(PATH, parts[1]);

    return attachmentInfo;
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
