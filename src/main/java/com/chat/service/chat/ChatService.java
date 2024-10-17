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
import com.chat.dto.channel.ChannelUserRelationInfoDto;
import com.chat.dto.chat.ChatAttachmentInfoDto;
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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
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
import javax.imageio.ImageIO;
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
  private final ServerUserRelationRepository serverUserRelationRepository;
  private final ChannelRepository channelRepository;
  private final ChannelUserRelationRepository channelUserRelationRepository;
  private final ChatRepository chatRepository;
  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;
  private final UUIDGenerator uuidGenerator;

  private static final String USER_UNREGISTERED = "USER:USER_UNREGISTERED";
  private static final String SERVER_NOT_FOUND = "SERVER:SERVER_NOT_FOUND";
  private static final String CHAT_NOT_FOUND = "CHAT:CHAT_NOT_FOUND";
  private static final String PAGE_INVALID = "VALID:PAGE_INVALID";
  private static final String CHANNEL_NOT_FOUND = "CHANNEL:CHANNEL_NOT_FOUND";
  private static final String CHANNEL_NOT_PARTICIPATED = "CHANNEL:CHANNEL_NOT_PARTICIPATED";
  private static final String UNSUPPORTED_FILE_TYPE = "CHAT:UNSUPPORTED_FILE_TYPE";

  private final SimpMessagingTemplate messagingTemplate;
  private static final String SUB_USER = "/sub/user/";
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
    Long serverId = messageDto.getServerId();
    Long channelId = messageDto.getChannelId();
    String message = messageDto.getMessage();
    String email = customUserDetailsService.getEmailByUserDetails();

    // validChannelUserRelation
    ChannelUserRelationInfoDto channelUserRelationInfoDto = this.validChannelUserRelation
        (serverId, channelId, email);
    Server server = serverId != null ? channelUserRelationInfoDto.getServer() : null;
    Channel channel = channelUserRelationInfoDto.getChannel();
    ChannelUserRelation channelUserRelation = channelUserRelationInfoDto.getChannelUserRelation();
    User user = channelUserRelationInfoDto.getUser();

    // attachment logic
    ChatAttachmentInfoDto chatAttachmentInfoDto = this.attachmentLogic(messageDto);
    String mimeType = chatAttachmentInfoDto != null ? chatAttachmentInfoDto.getMimeType() : null;
    String filePath = chatAttachmentInfoDto != null ? chatAttachmentInfoDto.getFilePath() : null;
    Integer attachmentWidth =
        chatAttachmentInfoDto != null ? chatAttachmentInfoDto.getAttachmentWidth() : null;
    Integer attachmentHeight =
        chatAttachmentInfoDto != null ? chatAttachmentInfoDto.getAttachmentHeight() : null;

    LocalDateTime createTime = LocalDateTime.now();
    // 메세지 저장
    Chat chat = Chat.builder()
        .server(server)
        .channel(channel)
        .user(user)
        .message(message)
        .attachmentType(mimeType)
        .attachment(filePath)
        .attachmentWidth(attachmentWidth)
        .attachmentHeight(attachmentHeight)
        .createTime(createTime)
        .updateTime(createTime)
        .build();

    this.directMessageLogic(channel, channelUserRelation, chat, user);

    // mention logic
    this.mentionLogic(server, channel, chat, user, message);

    // reply logic
    ChatInfoDto chatReferenceInfoDto = this.replyLogic(server, channel, chat, user, messageDto);
    chatRepository.save(chat);

    // record server, channel lastMessage
    Long chatId = chat.fetchChatIdForUpdateLastMessage();
    channel.updateLastMessageId(chatId);
    channelUserRelation.updateLastReadMessageId(chatId);
    channelRepository.save(channel);
    channelUserRelationRepository.save(channelUserRelation);

    // stomp pub
    String channelUrl = SUB_CHANNEL + channelId;
    String avatar = user.fetchAvatarForSendMessageResponse();
    MessageDto newMessageDto;
    newMessageDto = chat
        .buildMessageDtoForSendMessageResponse
            (messageDto, avatar, chatReferenceInfoDto);
    TransactionSynchronizationManager.registerSynchronization(
        new StompAfterCommitSynchronization(messagingTemplate, channelUrl, newMessageDto)
    );

    // 접속해있지만, 채널에 연결되어있지않은 유저에게 /user/{userId}로 메시지 발송
    List<Long> userIdList = channelUserRelationRepository
        .fetchUserIdListWhoConnectedButNotSubscribe(channel);
    userIdList.forEach(userId -> {
      String userUrl = SUB_USER + userId;
      TransactionSynchronizationManager.registerSynchronization(
          new StompAfterCommitSynchronization(messagingTemplate, userUrl, newMessageDto)
      );
    });

    // return
    Long id = chat.fetchChatIdForSendMessageResponse();
    return SendMessageResponseDto.builder()
        .serverId(serverId)
        .channelId(channelId)
        .id(id)
        .createTime(createTime)
        .avatar(avatar)
        .attachment(filePath)
        .attachmentWidth(attachmentWidth)
        .attachmentHeight(attachmentHeight)
        .build();
  }

  private ChannelUserRelationInfoDto validChannelUserRelation(Long serverId, Long channelId,
      String email) {
    ChannelUserRelationInfoDto channelUserRelationInfoDto = channelUserRelationRepository
        .fetchChannelUserRelationInfoDtoByServerIdAndChannelIdAndUserEmail
            (serverId, channelId, email);
    User user = channelUserRelationInfoDto.getUser();
    Server server = serverId != null ? channelUserRelationInfoDto.getServer() : null;
    Channel channel = channelUserRelationInfoDto.getChannel();
    ChannelUserRelation channelUserRelation = channelUserRelationInfoDto.getChannelUserRelation();
    if (serverId != null && server == null) {
      throw new ServerException(SERVER_NOT_FOUND);
    }
    if (channel == null) {
      throw new ChannelException(CHANNEL_NOT_FOUND);
    }
    if (channelUserRelation == null) {
      throw new ChannelException(CHANNEL_NOT_PARTICIPATED);
    }
    if (user == null) {
      throw new UserException(USER_UNREGISTERED);
    }
    return channelUserRelationInfoDto;
  }

  // attachmentLogic
  private ChatAttachmentInfoDto attachmentLogic(MessageDto messageDto) throws IOException {
    ChatAttachmentInfoDto chatAttachmentInfoDto = null;
    String attachment = messageDto.getAttachment();
    if (attachment != null) {
      String[] base64 = attachment.split(",");
      // mimeType -> data:image/jpeg,png,gif,bmp,webp, video/mp4,mpeg,ogg,
      // audio/mpeg,wav,ogg,mp4, text/plain, application/json,pdf,zip
      String metadata = base64[0];
      String base64Data = base64[1];
      String mimeType = metadata.split(":")[1].split(";")[0];  // image/png

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

      // jpg, png, bmp 스케일링
      if (mimeType.startsWith("image/jpeg") ||
          mimeType.startsWith("image/png") ||
          mimeType.startsWith("image/bmp")) {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(decode));

        BufferedImage scaledImage = scaleImage(originalImage);
        Integer scaledWidth = scaledImage.getWidth();
        Integer scaledHeight = scaledImage.getHeight();

        String originalFilename = uuidGenerator.generateUUID() + "_" + epochMilli;
        String originalFilePath = path + originalFilename + "." + extension;
        String scaledFileName = originalFilename
            + "&width=" + scaledWidth + "&height=" + scaledHeight;
        String scaledFilePath = path + scaledFileName + "." + extension;

        // 스케일링된 이미지를 파일로 저장
        File originalFile = new File(originalFilePath);
        File outputFile = new File(scaledFilePath);

        ImageIO.write(originalImage, extension, originalFile);
        ImageIO.write(scaledImage, extension, outputFile);

        chatAttachmentInfoDto = ChatAttachmentInfoDto.builder()
            .mimeType(mimeType)
            .filePath(scaledFilePath)
            .attachmentWidth(scaledWidth)
            .attachmentHeight(scaledHeight)
            .build();
      } else {
        String fileName = uuidGenerator.generateUUID() + "_" + epochMilli + "." + extension;
        String filePath = path + fileName;

        // 이미지가 아닌 파일은 그대로 저장
        Files.write(Paths.get(filePath), decode);

        chatAttachmentInfoDto = ChatAttachmentInfoDto.builder()
            .mimeType(mimeType)
            .filePath(filePath)
            .build();
      }
    }
    return chatAttachmentInfoDto;
  }

  private Map<String, String> getFileInfoFromMimeType(String mimeType) {
    // MIME 타입과 관련 정보 매핑
    Map<String, String> mimeTypeMapping = Map.ofEntries(
        Map.entry("audio/mpeg", "mp3:" + filePathChatAudio),
        Map.entry("audio/wav", "wav:" + filePathChatAudio),
        Map.entry("audio/mp4", "aac:" + filePathChatAudio),
        Map.entry("image/jpeg", "jpg:" + filePathChatImage),
        Map.entry("image/png", "png:" + filePathChatImage),
        Map.entry("image/gif", "gif:" + filePathChatImage),
        Map.entry("image/bmp", "bmp" + filePathChatImage),
        Map.entry("image/webp", "webp:" + filePathChatImage),
        Map.entry("text/plain", "txt:" + filePathChatText),
        Map.entry("video/mp4", "mp4:" + filePathChatVideo),
        Map.entry("video/mpeg", "mpeg:" + filePathChatVideo),
        Map.entry("video/ogg", "ogg:" + filePathChatVideo),
        Map.entry("application/json", "json:" + filePathChatJson),
        Map.entry("application/pdf", "pdf:" + filePathChatPdf),
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

  private BufferedImage scaleImage(BufferedImage originalImage) {
    int originalWidth = originalImage.getWidth();
    int originalHeight = originalImage.getHeight();

    int scaledWidth = originalWidth / 2;
    int scaledHeight = originalHeight / 2;

    Image scaledImage = originalImage.getScaledInstance(scaledWidth, scaledHeight,
        Image.SCALE_SMOOTH);

    BufferedImage outputImage = new BufferedImage(scaledWidth, scaledHeight,
        BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics2D = outputImage.createGraphics();
    graphics2D.drawImage(scaledImage, 0, 0, null);
    graphics2D.dispose();

    return outputImage;
  }

  private void directMessageLogic(Channel channel, ChannelUserRelation channelUserRelation,
      Chat chat, User user) {
    if (channelUserRelation.isDirectMessageChannel()) {
      // dm인 경우 notification 생성
      User mentionedUser = channelUserRelation.fetchMentionedUserForSendMessage();
      Notification notification = Notification.builder()
          .channel(channel)
          .chat(chat)
          .user(user)
          .mentionedUser(mentionedUser)
          .build();

      notificationRepository.save(notification);
    }
  }

  private void mentionLogic(Server server, Channel channel, Chat chat, User user, String message) {
    if (message == null) {
      return;
    }

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

  }

  // reply logic
  private ChatInfoDto replyLogic(Server server, Channel channel, Chat chat, User user,
      MessageDto messageDto) {
    Long chatReferenceId = messageDto.getChatReference();
    ChatInfoDto chatReferenceInfoDto = null;
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
      chatReferenceInfoDto = ChatInfoDto.builder()
          .id(chatReferenceInfo.getId())
          .username(chatReferenceInfo.getUsername())
          .avatarImageSmall(chatReferenceInfo.getAvatarImageSmall())
          .message(chatReferenceInfo.getMessage())
          .attachmentType(chatReferenceInfo.getAttachmentType())
          .build();
    }
    return chatReferenceInfoDto;
  }

  @Transactional
  public void updateMessage(MessageDto messageDto) {
    Long serverId = messageDto.getServerId();
    Long channelId = messageDto.getChannelId();
    Long chatId = messageDto.getChatId();
    String message = messageDto.getMessage();
    String email = customUserDetailsService.getEmailByUserDetails();

    ChannelUserRelationInfoDto channelUserRelationInfoDto = validChannelUserRelation
        (serverId, channelId, email);
    User user = channelUserRelationInfoDto.getUser();

    Chat chat = chatRepository.findByIdAndUserAndLogicDeleteFalse(chatId, user)
        .orElseThrow(() -> new ChatException(CHAT_NOT_FOUND));

    LocalDateTime updateTime = LocalDateTime.now();
    chat.updateMessage(messageDto, updateTime);
    chatRepository.save(chat);

    // stomp pub
    String channelUrl = SUB_CHANNEL + channelId;
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

  public ChatListResponseDto chatList(Long channelId) {
    String email = customUserDetailsService.getEmailByUserDetails();

    validChannelUserRelation(null, channelId, email);

    // 최근 50개 fetch
    List<ChatInfoDto> chatInfoDtoList = chatRepository.fetchChatInfoDtoListByChannelId(channelId);

    return ChatListResponseDto.builder()
        .chatList(chatInfoDtoList)
        .build();
  }

  public ChatListResponseDto chatListPrevious(Long channelId, Long chatId) {
    String email = customUserDetailsService.getEmailByUserDetails();

    validChannelUserRelation(null, channelId, email);

    // 주어진 chatId의 앞50개 fetch
    List<ChatInfoDto> chatInfoDtoList = chatRepository
        .fetchChatInfoDtoListByChannelIdAndChatId(channelId, chatId);

    return ChatListResponseDto.builder()
        .chatList(chatInfoDtoList)
        .build();
  }

  @Transactional
  public void delete(Long channelId, Long chatId) {
    String email = customUserDetailsService.getEmailByUserDetails();

    ChannelUserRelationInfoDto channelUserRelationInfoDto = validChannelUserRelation
        (null, channelId, email);
    User user = channelUserRelationInfoDto.getUser();

    Chat chat = chatRepository.findByIdAndUserAndLogicDeleteFalse(chatId, user)
        .orElseThrow(() -> new ChatException(CHAT_NOT_FOUND));

    chat.logicDelete();
    chatRepository.save(chat);

    // stomp pub
    String channelUrl = SUB_CHANNEL + channelId;
    MessageDto newMessageDto = MessageDto.builder()
        .messageType(MessageType.CHAT_DELETE)
        .channelId(channelId)
        .chatId(chatId)
        .build();
    messagingTemplate.convertAndSend(channelUrl, newMessageDto);
  }

  public ChatSearchResponseDto search(Long channelId, ChatSearchRequestDto requestDto,
      int page, int size) {
    String email = customUserDetailsService.getEmailByUserDetails();
    String keyword = requestDto.getKeyword();
    String username = requestDto.getUsername();
    String message = requestDto.getMessage();

    ChannelUserRelationInfoDto channelUserRelationInfoDto = this.validChannelUserRelation(null,
        channelId, email);
    Channel channel = channelUserRelationInfoDto.getChannel();

    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Direction.DESC, "id"));
    Page<ChatInfoDto> chatPage = null;

    if (!isEmpty(keyword)) {
      // 검색 기본쿼리
      chatPage = chatRepository.searchChatInfoDtoListDefault(channel, keyword, pageable);
    }

    if (isEmpty(keyword) && !isEmpty(username) && !isEmpty(message)) {
      // 둘을 검색하는 쿼리
      chatPage = chatRepository.searchChatInfoDtoListByUsernameAndMessage(channel, username,
          message, pageable);
    }

    if (isEmpty(keyword) && !isEmpty(username) && isEmpty(message)) {
      // 유저명으로만 검색
      chatPage = chatRepository.searchChatInfoDtoListByUsername(channel, username, pageable);
    }

    if (isEmpty(keyword) && isEmpty(username) && !isEmpty(message)) {
      // 내용만으로 검색
      chatPage = chatRepository.searchChatInfoDtoListByMessage(channel, message, pageable);
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
