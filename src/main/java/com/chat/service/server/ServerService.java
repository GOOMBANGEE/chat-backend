package com.chat.service.server;

import com.chat.domain.category.Category;
import com.chat.domain.category.CategoryUserRelation;
import com.chat.domain.channel.Channel;
import com.chat.domain.channel.ChannelUserRelation;
import com.chat.domain.chat.Chat;
import com.chat.domain.server.Server;
import com.chat.domain.server.ServerUserRelation;
import com.chat.domain.user.User;
import com.chat.dto.MessageDto;
import com.chat.dto.MessageDto.MessageType;
import com.chat.dto.category.CategoryInfoDto;
import com.chat.dto.channel.ChannelInfoDto;
import com.chat.dto.channel.ChannelRegistrationDto;
import com.chat.dto.server.ServerCreateRequestDto;
import com.chat.dto.server.ServerCreateResponseDto;
import com.chat.dto.server.ServerDeleteRequestDto;
import com.chat.dto.server.ServerInfoDto;
import com.chat.dto.server.ServerInviteInfoResponseDto;
import com.chat.dto.server.ServerInviteResponseDto;
import com.chat.dto.server.ServerJoinInfoDto;
import com.chat.dto.server.ServerJoinResponseDto;
import com.chat.dto.server.ServerListResponseDto;
import com.chat.dto.server.ServerSettingIconRequestDto;
import com.chat.dto.server.ServerSettingRequestDto;
import com.chat.dto.server.ServerUserInfoDto;
import com.chat.dto.server.ServerUserListResponseDto;
import com.chat.dto.user.UserInfo;
import com.chat.exception.ServerException;
import com.chat.exception.UserException;
import com.chat.repository.category.CategoryRepository;
import com.chat.repository.category.CategoryUserRelationRepository;
import com.chat.repository.channel.ChannelRepository;
import com.chat.repository.channel.ChannelUserRelationRepository;
import com.chat.repository.chat.ChatRepository;
import com.chat.repository.server.ServerRepository;
import com.chat.repository.server.ServerUserRelationRepository;
import com.chat.repository.user.UserRepository;
import com.chat.service.user.CustomUserDetailsService;
import com.chat.util.UUIDGenerator;
import com.chat.util.websocket.StompAfterCommitSynchronization;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ServerService {

  private final CustomUserDetailsService customUserDetailsService;
  private final ServerRepository serverRepository;
  private final ServerUserRelationRepository serverUserRelationRepository;
  private final CategoryRepository categoryRepository;
  private final CategoryUserRelationRepository categoryUserRelationRepository;
  private final ChannelRepository channelRepository;
  private final ChannelUserRelationRepository channelUserRelationRepository;
  private final ChatRepository chatRepository;
  private final UserRepository userRepository;

  private static final String USER_UNREGISTERED = "USER:USER_UNREGISTERED";
  private static final String SERVER_NOT_FOUND = "SERVER:SERVER_NOT_FOUND";
  private static final String SERVER_NAME_INVALID = "SERVER:SERVER_NAME_INVALID";
  private static final String SERVER_NOT_PARTICIPATED = "SERVER:SERVER_NOT_PARTICIPATED";
  private static final String SERVER_NOT_PERMITTED = "SERVER:SERVER_NOT_PERMITTED";
  private static final String SERVER_ALREADY_JOINED = "SERVER:SERVER_ALREADY_JOINED";
  private static final String SERVER_NOT_EMPTY = "SERVER:SERVER_NOT_EMPTY";
  private static final String UNSUPPORTED_FILE_TYPE = "SERVER:UNSUPPORTED_FILE_TYPE";
  private static final String IMAGE_SAVE_ERROR = "SERVER:IMAGE_SAVE_ERROR";

  private final UUIDGenerator uuidGenerator;
  private final SimpMessagingTemplate messagingTemplate;
  private static final String SUB_USER = "/sub/user/";
  private static final String SUB_SERVER = "/sub/server/";
  private static final String SUB_CHANNEL = "/sub/channel/";
  private final ObjectMapper mapper = new ObjectMapper();

  @Value("${server.front-url}")
  private String frontUrl;
  @Value("${server.file-path.server.icon}")
  private String filePathServerIcon;
  @Value("${server.time-zone}")
  private String timeZone;

  // 서버 생성
  @Transactional
  public ServerCreateResponseDto create(
      ServerCreateRequestDto requestDto) throws IOException {
    // 등록된 유저인지 확인
    String email = customUserDetailsService.getEmailByUserDetails();
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    // 서버 생성
    String username = requestDto.getUsername();
    String name = requestDto.getName();
    String icon = requestDto.getIcon();

    String iconFilePath = this.serverIconScaling(icon);

    Server server = Server.builder()
        .name(name)
        .icon(iconFilePath)
        .ownerUsername(username)
        .userCount(1L)
        .logicDelete(false)
        .build();

    ServerUserRelation serverUserRelation = ServerUserRelation.builder()
        .server(server)
        .user(user)
        .owner(true)
        .build();

    serverRepository.save(server);
    serverUserRelationRepository.save(serverUserRelation);

    // 기본 카테고리 생성
    Category category = Category.builder()
        .name("채팅 채널")
        .displayOrder(1024.0)
        .open(true)
        .logicDelete(false)
        .server(server)
        .build();
    categoryRepository.save(category);

    CategoryUserRelation categoryUserRelation = CategoryUserRelation.builder()
        .category(category)
        .user(user)
        .readMessage(true)
        .writeMessage(true)
        .viewHistory(true)
        .build();
    categoryUserRelationRepository.save(categoryUserRelation);

    // 기본채널 생성
    Channel channel = Channel.builder()
        .name("일반")
        .displayOrder(1024.0)
        .open(true)
        .server(server)
        .category(category)
        .build();
    channelRepository.save(channel);

    ChannelUserRelation channelUserRelation = ChannelUserRelation.builder()
        .channel(channel)
        .user(user)
        .readMessage(true)
        .writeMessage(true)
        .viewHistory(true)
        .build();
    channelUserRelationRepository.save(channelUserRelation);

    Long id = server.getServerIdForServerCreateResponse();
    Long categoryId = category.getCategoryIdForServerCreateResponse();
    Long channelId = channel.getChannelIdForServerCreate();
    server.setDefaultChannel(channel);
    serverRepository.save(server);

    LocalDateTime createTime = LocalDateTime.now(ZoneId.of(timeZone));
    Chat chat = Chat.builder()
        .server(server)
        .channel(channel)
        .user(user)
        .enter(true)
        .createTime(createTime)
        .build();
    chatRepository.save(chat);

    Long chatId = chat.fetchChatIdForUpdateLastMessage();
    channel.updateLastMessageId(chatId);
    channelRepository.save(channel);

    // message queue 활성화를 위한 dummy message
    Long userId = requestDto.getUserId();
    MessageDto newMessageDto = MessageDto.builder()
        .messageType(MessageType.SERVER_CREATE)
        .serverId(id)
        .categoryId(categoryId)
        .channelId(channelId)
        .userId(userId)
        .build();
    List<String> urlList = List.of(
        SUB_USER + userId,
        SUB_SERVER + id,
        SUB_CHANNEL + channelId
    );
    urlList.forEach(url ->
        TransactionSynchronizationManager.registerSynchronization(
            new StompAfterCommitSynchronization(messagingTemplate, url, newMessageDto)
        )
    );

    return ServerCreateResponseDto.builder()
        .id(id)
        .name(name)
        .icon(iconFilePath)
        .categoryId(categoryId)
        .categoryName("채팅 채널")
        .categoryDisplayOrder(1024.0)
        .channelId(channelId)
        .channelName("일반")
        .channelDisplayOrder(1024.0)
        .build();
  }

  // 참여중인 서버 목록
  public ServerListResponseDto list() {
    // 해당 유저가 속한 서버 리턴
    String email = customUserDetailsService.getEmailByUserDetails();
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    List<ServerInfoDto> serverInfoDtoList = serverUserRelationRepository
        .fetchServerInfoDtoListByUser(user);

    // 유저가 접근할 권한이 있는 카테고리
    List<CategoryInfoDto> categoryInfoDtoList = categoryUserRelationRepository
        .fetchCategoryInfoDtoListByUser(user);

    // 유저가 접근할 권한이 있는 채널
    List<ChannelInfoDto> channelInfoDtoList = channelUserRelationRepository
        .fetchChannelInfoDtoListByUser(user);

    List<ChannelInfoDto> directMessageChannelInfoDtoList = channelUserRelationRepository
        .fetchDirectMessageChannelInfoDtoListByUser(user);

    return ServerListResponseDto.builder()
        .serverList(serverInfoDtoList)
        .categoryList(categoryInfoDtoList)
        .channelList(channelInfoDtoList)
        .directMessageChannelList(directMessageChannelInfoDtoList)
        .build();
  }

  // 서버 설정변경
  @Transactional
  public void setting(Long serverId, ServerSettingRequestDto requestDto)
      throws JsonProcessingException {
    String email = customUserDetailsService.getEmailByUserDetails();

    // 해당 서버 참여자인지 확인
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    Server server = serverRepository.findByIdAndLogicDeleteFalse(serverId)
        .orElseThrow(() -> new ServerException(SERVER_NOT_FOUND));

    // 서버의 주인인지 확인
    ServerUserRelation serverUserRelation = serverUserRelationRepository
        .findByUserAndServerAndLogicDeleteFalse(user, server)
        .orElseThrow(() -> new ServerException(SERVER_NOT_PARTICIPATED));

    // 주인이 아닌경우 권한없음
    if (serverUserRelation.isOwner()) {
      String name = requestDto.getName();
      server.changeServerName(name);
      serverRepository.save(server);

      // stomp pub
      String serverUrl = SUB_SERVER + serverId;
      ServerInfoDto serverInfoDto = ServerInfoDto.builder()
          .id(serverId)
          .name(name)
          .build();
      MessageDto newMessageDto = MessageDto.builder()
          .messageType(MessageType.SERVER_UPDATE_NAME)
          .serverId(serverId)
          .message(mapper.writeValueAsString(serverInfoDto))
          .build();
      messagingTemplate.convertAndSend(serverUrl, newMessageDto);
      return;
    }
    throw new ServerException(SERVER_NOT_PERMITTED);
  }

  // 서버 아이콘 변경
  @Transactional
  public void settingIcon(Long serverId, ServerSettingIconRequestDto requestDto)
      throws IOException {
    String email = customUserDetailsService.getEmailByUserDetails();

    // 해당 서버 참여자인지 확인
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    Server server = serverRepository.findByIdAndLogicDeleteFalse(serverId)
        .orElseThrow(() -> new ServerException(SERVER_NOT_FOUND));

    // 서버의 주인인지 확인
    ServerUserRelation serverUserRelation = serverUserRelationRepository
        .findByUserAndServerAndLogicDeleteFalse(user, server)
        .orElseThrow(() -> new ServerException(SERVER_NOT_PARTICIPATED));

    // 주인이 아닌경우 권한없음
    if (serverUserRelation.isOwner()) {
      String icon = requestDto.getIcon();
      String filePath = this.serverIconScaling(icon);
      server.changeServerIcon(filePath);
      serverRepository.save(server);

      // stomp pub
      ServerInfoDto serverInfoDto = ServerInfoDto.builder()
          .id(serverId)
          .icon(filePath)
          .build();
      MessageDto newMessageDto = MessageDto.builder()
          .messageType(MessageType.SERVER_UPDATE_ICON)
          .serverId(serverId)
          .message(mapper.writeValueAsString(serverInfoDto))
          .build();

      List<Long> userIdList = serverUserRelationRepository
          .fetchUserIdListByServerAndServerDeleteFalseAndLogicDeleteFalse(server);
      userIdList.forEach(userId -> {
        String userUrl = SUB_USER + userId;
        TransactionSynchronizationManager.registerSynchronization(
            new StompAfterCommitSynchronization(messagingTemplate, userUrl, newMessageDto)
        );
      });

      return;
    }
    throw new ServerException(SERVER_NOT_PERMITTED);
  }

  private String serverIconScaling(String icon) throws IOException {
    if (icon != null) {
      String[] base64 = icon.split(",");
      String metadata = base64[0];
      String base64Data = base64[1];
      String mimeType = metadata.split(":")[1].split(";")[0];

      // 이미지 확장자 추출
      String extension = getFileExtensionFromMimeType(mimeType);
      if (extension == null) {
        throw new ServerException(UNSUPPORTED_FILE_TYPE);
      }

      // base64 데이터를 바이트 배열로 디코딩
      byte[] decode = Base64.getDecoder().decode(base64Data);

      // 현재 시간 millisecond
      ZoneId zoneid = ZoneId.of(timeZone);
      long epochMilli = LocalDateTime.now().atZone(zoneid).toInstant().toEpochMilli();

      String fileName = uuidGenerator.generateUUID() + "_" + epochMilli + "." + extension;
      String filePath = filePathServerIcon + fileName;

      BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(decode));

      try {
        // 이미지 스케일링 후 저장 (작은 이미지)
        Thumbnails.of(originalImage)
            .size(56, 56)
            .toFile(new File(filePath));
      } catch (IOException e) {
        throw new ServerException(IMAGE_SAVE_ERROR);
      }
      return filePath;
    }
    return null;
  }

  // base64 문자열에서 이미지 확장자 추출
  private String getFileExtensionFromMimeType(String mimeType) {
    if (mimeType.startsWith("image/jpeg")) {
      return "jpg";
    } else if (mimeType.startsWith("image/png")) {
      return "png";
    } else if (mimeType.startsWith("image/gif")) {
      return "gif";
    } else {
      return null;  // 지원하지 않는 파일 형식
    }
  }

  // 서버 입장
  @Transactional
  public ServerJoinResponseDto join(String code) {
    String email = customUserDetailsService.getEmailByUserDetails();

    // 해당 서버 참여자인지 확인
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    ServerJoinInfoDto serverJoinInfoDto = serverRepository.fetchServerInfoDtoByServerCode(code);
    Server server = serverJoinInfoDto.getServer();

    Optional<ServerUserRelation> serverUserRelation = serverUserRelationRepository.findServerUserRelationByUserAndServer(
        user, server);
    // serverUserRelation값이 존재하는가?
    if (serverUserRelation.isPresent()) {
      // logicDelete 상태이면 재입장
      if (serverUserRelation.get().isLogicDelete()) {
        serverUserRelation.get().reJoin();
        serverUserRelationRepository.save(serverUserRelation.get());
      } else {
        // logicDelete == false -> 이미 참여중 에러
        throw new ServerException(SERVER_ALREADY_JOINED);
      }
    }

    // 이전에 가입하지 않았던 새로운 유저 입장
    if (serverUserRelation.isEmpty()) {
      ServerUserRelation newServerUserRelation = ServerUserRelation.builder()
          .server(server)
          .user(user)
          .build();
      serverUserRelationRepository.save(newServerUserRelation);
    }

    // server에 userCount +1
    server.userJoin();
    serverRepository.save(server);

    // server내의 open카테고리 등록설정
    List<Category> categoryList = categoryRepository
        .findByServer(server);
    List<CategoryUserRelation> categoryUserRelationList = new ArrayList<>();
    categoryList.forEach(
        (category -> {
          CategoryUserRelation categoryUserRelation = CategoryUserRelation.builder()
              .category(category)
              .user(user)
              .readMessage(true)
              .writeMessage(true)
              .viewHistory(true)
              .build();

          categoryUserRelationList.add(categoryUserRelation);
        })
    );
    categoryUserRelationRepository.saveAll(categoryUserRelationList);

    // server내의 open채널 등록설정
    List<ChannelRegistrationDto> channelRegistrationDtoList = channelRepository
        .fetchChannelRegistrationDtoListByServer(server);
    List<ChannelUserRelation> channelUserRelationList = new ArrayList<>();
    channelRegistrationDtoList.forEach(
        (channelRegistrationDto -> {
          Channel channel = channelRegistrationDto.getChannel();
          Long lastMessageId = channelRegistrationDto.getLastMessageId();

          ChannelUserRelation channelUserRelation = ChannelUserRelation.builder()
              .channel(channel)
              .user(user)
              .readMessage(true)
              .writeMessage(true)
              .viewHistory(true)
              .lastReadMessageId(lastMessageId)
              .build();

          channelUserRelationList.add(channelUserRelation);
        })
    );
    channelUserRelationRepository.saveAll(channelUserRelationList);

    // return 입장한 서버 id
    Long channelId = serverJoinInfoDto.getChannelId();
    ServerJoinResponseDto responseDto = server.getServerIdForServerJoinResponse(channelId);
    Channel channel = serverJoinInfoDto.getChannel();
    LocalDateTime createTime = LocalDateTime.now(ZoneId.of(timeZone));
    // 서버 입장 메시지 전송
    Chat chat = Chat.builder()
        .server(server)
        .channel(channel)
        .user(user)
        .enter(true)
        .createTime(createTime)
        .build();
    chatRepository.save(chat);

    Long serverId = responseDto.getId();

    UserInfo userInfo = user.fetchUserInfoForServerJoinResponse();
    Long userId = userInfo.getId();
    String username = userInfo.getUsername();

    String channelUrl = SUB_CHANNEL + channelId;
    MessageDto newMessageDto = chat
        .buildMessageDtoForSeverJoinResponse(serverId, channelId, userId, username);
    messagingTemplate.convertAndSend(channelUrl, newMessageDto);

    // 접속해있지만, 채널에 연결되어있지않은 유저에게 /user/{userId}로 메시지 발송
    List<Long> userIdList = channelUserRelationRepository
        .fetchUserIdListWhoConnectedButNotSubscribe(channel);
    userIdList.forEach(userIdInList -> {
      String userUrl = SUB_USER + userIdInList;
      TransactionSynchronizationManager.registerSynchronization(
          new StompAfterCommitSynchronization(messagingTemplate, userUrl, newMessageDto)
      );
    });

    return responseDto;
  }

  // 서버 초대코드 조회
  public ServerInviteInfoResponseDto inviteInfo(String code) {
    String email = customUserDetailsService.getEmailByUserDetails();

    // 해당 서버 참여자인지 확인
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    Server server = serverRepository.findByCodeAndLogicDeleteFalse(code)
        .orElseThrow(() -> new ServerException(SERVER_NOT_FOUND));

    boolean isAlreadyJoined = serverUserRelationRepository
        .findByUserAndServerAndLogicDeleteFalse(user, server)
        .isPresent();
    if (isAlreadyJoined) {
      throw new ServerException(SERVER_ALREADY_JOINED);
    }

    return server.buildServerInviteInfoResponseDto();
  }

  // 서버 초대코드 생성
  @Transactional
  public ServerInviteResponseDto invite(Long serverId) {
    String email = customUserDetailsService.getEmailByUserDetails();

    // 해당 서버 참여자인지 확인
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    Server server = serverUserRelationRepository.fetchServerByUserAndServerId(user, serverId)
        .orElseThrow(() -> new ServerException(SERVER_NOT_FOUND));

    // invite code -> expire 7d default
    // invite code가 있는 경우 -> 기존 code fetch
    if (server.isPresentInviteCode()) {
      String code = server.fetchInviteCode();
      String link = frontUrl + "/invite/" + code;

      return ServerInviteResponseDto.builder()
          .link(link)
          .build();
    }

    // invite code가 없는 경우 -> 생성 후 fetch
    String code = generateUniqueCode();
    String link = frontUrl + "/invite/" + code;
    server.createInviteCode(code);
    serverRepository.save(server);

    return ServerInviteResponseDto.builder()
        .link(link)
        .build();
  }

  private String generateUniqueCode() {
    String code;
    do {
      code = randomAlphaNumeric(8);
    } while (serverRepository.findByCodeAndLogicDeleteFalse(code).isPresent());
    return code;
  }

  private String randomAlphaNumeric(int length) {
    final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    SecureRandom random = new SecureRandom();
    StringBuilder builder = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      builder.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
    }
    return builder.toString();
  }

  @Transactional
  public void leave(Long serverId) {
    String email = customUserDetailsService.getEmailByUserDetails();

    // 해당 서버 참여자인지 확인
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    Server server = serverUserRelationRepository.fetchServerByUserAndServerId(user, serverId)
        .orElseThrow(() -> new ServerException(SERVER_NOT_FOUND));

    ServerUserRelation serverUserRelation = serverUserRelationRepository
        .findByUserAndServerAndLogicDeleteFalse(user, server)
        .orElseThrow(() -> new ServerException(SERVER_NOT_PARTICIPATED));

    // 서버 주인인 경우 유저가 남아있는지 체크
    if (serverUserRelation.isOwner() &&
        serverUserRelationRepository.findByServerAndOwnerFalseAndLogicDeleteFalse(server)
            .isPresent()) {
      // 유저가 남아있다면 주인은 나갈 수 없다
      throw new ServerException(SERVER_NOT_EMPTY);
    }

    // 유저가 모두 나간경우 server 삭제처리
    Long userCount = server.userLeave();
    if (userCount == 0) {
      server.logicDelete();
      serverRepository.save(server);
    }
    serverUserRelation.logicDelete();
    serverRepository.save(server);
    serverUserRelationRepository.save(serverUserRelation);

    // 나간 유저는 ChannelUserRelation에서 삭제
    List<ChannelUserRelation> channelUserRelationList = channelUserRelationRepository
        .fetchChannelUserRelationListByServerAndUser(server, user);
    channelUserRelationRepository.deleteAll(channelUserRelationList);

    String serverUrl = SUB_SERVER + serverId;
    Long userId = user.fetchUserIdForServerLeaveResponse();
    MessageDto newMessageDto = MessageDto.builder()
        .messageType(MessageType.SERVER_LEAVE)
        .serverId(serverId)
        .userId(userId)
        .build();
    messagingTemplate.convertAndSend(serverUrl, newMessageDto);
  }

  // 서버 삭제
  @Transactional
  public void delete(Long serverId, ServerDeleteRequestDto requestDto) {
    String email = customUserDetailsService.getEmailByUserDetails();

    // 해당 서버 참여자인지 확인
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    Server server = serverRepository.findByIdAndLogicDeleteFalse(serverId)
        .orElseThrow(() -> new ServerException(SERVER_NOT_FOUND));

    String name = requestDto.getName();
    if (!server.checkName(name)) {
      throw new ServerException(SERVER_NAME_INVALID);
    }

    // 서버의 주인인지 확인
    ServerUserRelation serverUserRelation = serverUserRelationRepository
        .findByUserAndServerAndLogicDeleteFalse(user, server)
        .orElseThrow(() -> new ServerException(SERVER_NOT_PARTICIPATED));

    // 주인이 아닌경우 권한없음
    if (serverUserRelation.isOwner()) {
      server.logicDelete();
      serverRepository.save(server);

      // stomp pub
      String serverUrl = SUB_SERVER + serverId;
      MessageDto newMessageDto = MessageDto.builder()
          .messageType(MessageType.SERVER_DELETE)
          .serverId(serverId)
          .build();
      messagingTemplate.convertAndSend(serverUrl, newMessageDto);
    }
    throw new ServerException(SERVER_NOT_PERMITTED);
  }

  public ServerUserListResponseDto userList(Long serverId) {
    // serverUserRelation에 존재하는 모든 항목 가져오기
    String email = customUserDetailsService.getEmailByUserDetails();

    // 해당 서버 참여자인지 확인
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));
    Server server = serverRepository.findByIdAndLogicDeleteFalse(serverId)
        .orElseThrow(() -> new ServerException(SERVER_NOT_FOUND));

    // 서버에 속해있는 유저 정보
    List<ServerUserInfoDto> serverUserInfoDtoList = serverUserRelationRepository
        .fetchServerUserInfoDtoListByUserAndServer(user, server);

    if (serverUserInfoDtoList.isEmpty()) {
      throw new ServerException(SERVER_NOT_PARTICIPATED);
    }

    return ServerUserListResponseDto.builder()
        .serverUserInfoDtoList(serverUserInfoDtoList)
        .build();
  }
}
