package com.chat.service.channel;

import com.chat.domain.category.Category;
import com.chat.domain.channel.Channel;
import com.chat.domain.channel.ChannelServerRoleRelation;
import com.chat.domain.channel.ChannelUserRelation;
import com.chat.domain.server.Server;
import com.chat.domain.server.ServerRole;
import com.chat.domain.server.ServerUserRelation;
import com.chat.domain.user.Notification;
import com.chat.domain.user.User;
import com.chat.dto.MessageDto;
import com.chat.dto.MessageDto.MessageType;
import com.chat.dto.channel.ChannelCreateRequestDto;
import com.chat.dto.channel.ChannelCreateResponseDto;
import com.chat.dto.channel.ChannelRenameRequestDto;
import com.chat.dto.channel.ChannelSettingRequestDto;
import com.chat.dto.channel.ChannelSettingResponseDto;
import com.chat.dto.channel.ChannelUserRelationInfoDto;
import com.chat.dto.channel.DirectMessageChannelCreateResponseDto;
import com.chat.dto.user.UserInfo;
import com.chat.exception.CategoryException;
import com.chat.exception.ChannelException;
import com.chat.exception.ChatException;
import com.chat.exception.ServerException;
import com.chat.exception.UserException;
import com.chat.repository.category.CategoryRepository;
import com.chat.repository.channel.ChannelQueryRepository;
import com.chat.repository.channel.ChannelRepository;
import com.chat.repository.channel.ChannelServerRoleRelationRepository;
import com.chat.repository.channel.ChannelUserRelationQueryRepository;
import com.chat.repository.channel.ChannelUserRelationRepository;
import com.chat.repository.chat.ChatRepository;
import com.chat.repository.server.ServerRepository;
import com.chat.repository.server.ServerRoleRepository;
import com.chat.repository.server.ServerRoleUserRelationQueryRepository;
import com.chat.repository.server.ServerUserRelationQueryRepository;
import com.chat.repository.server.ServerUserRelationRepository;
import com.chat.repository.user.NotificationRepository;
import com.chat.repository.user.UserRepository;
import com.chat.service.user.CustomUserDetailsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChannelService {

  private final CustomUserDetailsService customUserDetailsService;
  private final ServerRepository serverRepository;
  private final ServerRoleRepository serverRoleRepository;
  private final ServerUserRelationRepository serverUserRelationRepository;
  private final ServerUserRelationQueryRepository serverUserRelationQueryRepository;
  private final ServerRoleUserRelationQueryRepository serverRoleUserRelationQueryRepository;
  private final CategoryRepository categoryRepository;
  private final ChannelRepository channelRepository;
  private final ChannelQueryRepository channelQueryRepository;
  private final ChannelServerRoleRelationRepository channelServerRoleRelationRepository;
  private final ChannelUserRelationRepository channelUserRelationRepository;
  private final ChannelUserRelationQueryRepository channelUserRelationQueryRepository;
  private final ChatRepository chatRepository;
  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  private static final String USER_UNREGISTERED = "USER:USER_UNREGISTERED";
  private static final String SERVER_NOT_FOUND = "SERVER:SERVER_NOT_FOUND";
  private static final String CATEGORY_NOT_FOUND = "CATEGORY:CATEGORY_NOT_FOUND";
  private static final String SERVER_NOT_PARTICIPATED = "SERVER:SERVER_NOT_PARTICIPATED";
  private static final String NO_CHANNEL_CREATE_PERMISSION = "SERVER:NO_CHANNEL_CREATE_PERMISSION";
  private static final String CHANNEL_NOT_FOUND = "CHANNEL:CHANNEL_NOT_FOUND";
  private static final String CHANNEL_ALREADY_EXIST = "CHANNEL:CHANNEL_ALREADY_EXIST";
  private static final String CHANNEL_NOT_PARTICIPATED = "CHANNEL:CHANNEL_NOT_PARTICIPATED";
  private static final String CHAT_NOT_FOUND = "CHAT:CHAT_NOT_FOUND";

  private final SimpMessagingTemplate messagingTemplate;
  private static final String SUB_SERVER = "/sub/server/";
  private static final String SUB_USER = "/sub/user/";
  private final ObjectMapper mapper = new ObjectMapper();


  @Transactional
  public ChannelCreateResponseDto create(ChannelCreateRequestDto requestDto)
      throws JsonProcessingException {
    // 등록된 유저인지 확인
    String email = customUserDetailsService.getEmailByUserDetails();
    User user = userRepository
        .findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    Long serverId = requestDto.getServerId();
    // dmChannelLogic
    if (serverId == null) {
      // 이미 채널이 있는경우 만들지않음 -> 두 유저가 관계를 가지고있는 채널이있다면 exception
      Long userId = requestDto.getUserId();
      User mentionedUser = userRepository.findByIdAndLogicDeleteFalse(userId)
          .orElseThrow(() -> new UserException(USER_UNREGISTERED));
      if (channelUserRelationQueryRepository
          .searchDirectMessageChannel(user, mentionedUser)
          .isPresent()) {
        throw new ChannelException(CHANNEL_ALREADY_EXIST);
      }

      Channel channel = Channel.builder()
          .open(true)
          .build();
      channelRepository.save(channel);

      // 보내는유저와 받는유저에 대해서 channelUserRelation 추가
      List<ChannelUserRelation> channelUserRelationList = new ArrayList<>();
      ChannelUserRelation channelUserRelation1 = ChannelUserRelation.builder()
          .channel(channel)
          .user(user)
          .userDirectMessage(mentionedUser)
          .readMessage(true)
          .writeMessage(true)
          .viewHistory(true)
          .build();
      ChannelUserRelation channelUserRelation2 = ChannelUserRelation.builder()
          .channel(channel)
          .user(mentionedUser)
          .userDirectMessage(user)
          .readMessage(true)
          .writeMessage(true)
          .viewHistory(true)
          .build();
      channelUserRelationList.add(channelUserRelation1);
      channelUserRelationList.add(channelUserRelation2);

      channelUserRelationRepository.saveAll(channelUserRelationList);
      Long channelId = channel.getChannelIdForChannelCreate();
      UserInfo userInfo = user.fetchUserInfoForDirectMessageChannelCreate();
      UserInfo mentionedUserInfo = mentionedUser.fetchUserInfoForDirectMessageChannelCreate();
      DirectMessageChannelCreateResponseDto stompResponseDto = DirectMessageChannelCreateResponseDto.builder()
          .id(channelId)
          .userId(userInfo.getId())
          .username(userInfo.getUsername())
          .avatar(userInfo.getAvatar())
          .build();

      // dm 받는 유저에게 채널생성알림
      String userUrl = SUB_USER + userId;
      MessageDto newMessageDto = MessageDto.builder()
          .messageType(MessageType.CHANNEL_CREATE_DIRECT_MESSAGE)
          .message(mapper.writeValueAsString(stompResponseDto))
          .build();
      messagingTemplate.convertAndSend(userUrl, newMessageDto);

      // dm채널 생성유저에게 dm받는 유저의 정보 제공
      return ChannelCreateResponseDto.builder()
          .id(channelId)
          .mentionedUserId(mentionedUserInfo.getId())
          .mentionedUsername(mentionedUserInfo.getUsername())
          .mentionedUserAvatar(mentionedUserInfo.getAvatar())
          .build();
    } else {
      Server server = serverRepository
          .findByIdAndLogicDeleteFalse(serverId)
          .orElseThrow(() -> new ServerException(SERVER_NOT_FOUND));
      ServerUserRelation serverUserRelation = serverUserRelationRepository
          .findByUserAndServerAndLogicDeleteFalse(user, server)
          .orElseThrow(() -> new ServerException(SERVER_NOT_PARTICIPATED));
      List<ServerRole> serverRoleUserRelation = serverRoleUserRelationQueryRepository
          .fetchServerRoleListByServerAndUser(server, user);

      Long categoryId = requestDto.getCategoryId();
      Category category = categoryRepository.findByIdAndLogicDeleteFalse(categoryId)
          .orElseThrow(() -> new CategoryException(CATEGORY_NOT_FOUND));

      // 채널 생성 권한 확인
      // 서버의 주인이거나, 역할 중 하나가 채널 생성 권한이 있는지 확인
      boolean authorized = serverUserRelation.isOwner() ||
          serverRoleUserRelation
              .stream()
              .anyMatch(ServerRole::checkCreateChannel);

      // 권한이 없을 경우 예외처리
      if (!authorized) {
        throw new ServerException(NO_CHANNEL_CREATE_PERMISSION);
      }

      // 만약 역할, 유저를 지정한 경우 해당 역할, 유저에게 읽기 권한 부여
      List<Long> allowRoleIdList = requestDto.getAllowRoleIdList();
      List<Long> allowUserIdList = requestDto.getAllowUserIdList();

      String name = requestDto.getName();
      Double displayOrder = channelQueryRepository.fetchMaxDisplayOrderByCategory(category) * 2;
      boolean open = allowRoleIdList == null && allowUserIdList == null;

      Channel channel = Channel.builder()
          .name(name)
          .displayOrder(displayOrder)
          .open(open)
          .server(server)
          .category(category)
          .build();
      channelRepository.save(channel);

      // 공개 채널인 경우 서버에 참가중인 모든 유저를 ChannelUserRelation에 추가
      if (open) {
        List<User> userList = serverUserRelationQueryRepository.fetchUserListByServer(server);
        List<ChannelUserRelation> channelUserRelationList = new ArrayList<>();
        userList.forEach(
            serverUser -> {
              ChannelUserRelation channelUserRelation = ChannelUserRelation.builder()
                  .channel(channel)
                  .user(serverUser)
                  .readMessage(true)
                  .writeMessage(true)
                  .viewHistory(true)
                  .build();
              channelUserRelationList.add(channelUserRelation);
            }
        );
        channelUserRelationRepository.saveAll(channelUserRelationList);
      }

      if (allowRoleIdList != null) {
        // 여러 역할 한번에 조회
        List<ServerRole> serverRoleList = serverRoleRepository.findByIdInAndLogicDeleteFalse(
            allowRoleIdList);
        // 조회된 역할들 순회하면서 ChannelServerRoleRelation 생성
        List<ChannelServerRoleRelation> channelServerRoleRelationList = serverRoleList.stream()
            .map(serverRole -> ChannelServerRoleRelation.builder()
                .channel(channel)
                .serverRole(serverRole)
                .readMessage(true)
                .writeMessage(true)
                .viewHistory(true)
                .build())
            .toList();
        // 한번에 저장
        channelServerRoleRelationRepository.saveAll(channelServerRoleRelationList);

        // 서버에 해당 역할을 가진 유저 조회
        List<User> userList = serverRoleUserRelationQueryRepository
            .fetchUserByServerRoleIn(serverRoleList);
        List<ChannelUserRelation> channelUserRelationList = new ArrayList<>();
        // 해당 역할을 가진 유저들을 순회하면서 ChannelUserRelation 생성
        userList.forEach(serverRoleUser -> {
          ChannelUserRelation channelUserRelation = ChannelUserRelation.builder()
              .channel(channel)
              .user(serverRoleUser)
              .readMessage(true)
              .writeMessage(true)
              .viewHistory(true)
              .build();
          channelUserRelationList.add(channelUserRelation);
        });
        channelUserRelationRepository.saveAll(channelUserRelationList);
      }

      if (allowUserIdList != null) {
        // 여러 유저 한번에 조회
        List<User> userList = userRepository.findByIdInAndLogicDeleteFalse(allowUserIdList);
        // 조회된 유저들 순회하면서 ChannelUserRelation 생성
        List<ChannelUserRelation> channelUserRelationList = userList.stream()
            .map(userInList -> ChannelUserRelation.builder()
                .channel(channel)
                .user(userInList)
                .readMessage(true)
                .writeMessage(true)
                .viewHistory(true)
                .build())
            .toList();
        // 한번에 저장
        channelUserRelationRepository.saveAll(channelUserRelationList);
      }

      Long channelId = channel.getChannelIdForChannelCreate();
      ChannelCreateResponseDto responseDto = ChannelCreateResponseDto.builder()
          .id(channelId)
          .name(name)
          .displayOrder(displayOrder)
          .serverId(serverId)
          .categoryId(categoryId)
          .allowRoleIdList(allowRoleIdList)
          .allowUserIdList(allowUserIdList)
          .build();
      // stomp pub
      String serverUrl = SUB_SERVER + serverId;
      MessageDto newMessageDto = MessageDto.builder()
          .messageType(MessageType.CHANNEL_CREATE)
          .serverId(serverId)
          .message(mapper.writeValueAsString(responseDto))
          .build();
      messagingTemplate.convertAndSend(serverUrl, newMessageDto);
      return responseDto;
    }
  }

  @Transactional
  public void rename(Long serverId, Long channelId, ChannelRenameRequestDto requestDto)
      throws JsonProcessingException {
    Channel channel = channelRepository.findByIdAndLogicDeleteFalseAndServerId(channelId, serverId)
        .orElseThrow(() -> new ChannelException(CHANNEL_NOT_FOUND));

    String name = requestDto.getName();
    channel.rename(name);
    channelRepository.save(channel);

    // 채널 설정변경 메시지 발송
    ChannelSettingResponseDto channelSettingResponseDto = ChannelSettingResponseDto.builder()
        .id(channelId)
        .name(requestDto.getName())
        .build();
    String serverUrl = SUB_SERVER + serverId;
    MessageDto newMessageDto = MessageDto.builder()
        .messageType(MessageType.CHANNEL_UPDATE)
        .serverId(serverId)
        .channelId(channelId)
        .message(mapper.writeValueAsString(channelSettingResponseDto))
        .build();
    messagingTemplate.convertAndSend(serverUrl, newMessageDto);
  }

  @Transactional
  public void setting(Long serverId, Long channelId, ChannelSettingRequestDto requestDto)
      throws JsonProcessingException {
    // serverId와 channelId가 맞는지 확인
    Channel channel = channelRepository.findByIdAndLogicDeleteFalseAndServerId(channelId, serverId)
        .orElseThrow(() -> new ChannelException(CHANNEL_NOT_FOUND));

    boolean open = requestDto.isOpen();
    List<Long> allowRoleIdList = requestDto.getAllowRoleIdList();
    List<Long> allowUserIdList = requestDto.getAllowUserIdList();
    if (open && allowRoleIdList == null && allowUserIdList == null) {
      // open 설정시 기존 ChannelServerRoleRelation 모두 삭제
      channelServerRoleRelationRepository.bulkDeleteByChannelId(channelId);

      // 서버에 속한 모든 유저 ChannelUserRelation 등록 (기존에 등록되어있는 유저는 제외)
      // 이미 ChannelUserRelation에 등록된 유저 조회
      List<User> existingUsers = channelUserRelationQueryRepository.fetchUserListByChannel(channel);

      // 서버에 등록된 모든 유저 조회
      Server server = serverRepository.findByIdAndLogicDeleteFalse(serverId)
          .orElseThrow(() -> new ServerException(SERVER_NOT_FOUND));
      List<User> userList = serverUserRelationQueryRepository.fetchUserListByServer(server);

      // 기존에 등록된 유저를 제외한 나머지 유저
      List<User> usersToAdd = userList.stream()
          .filter(user -> !existingUsers.contains(user))
          .toList();

      // 추가할 유저들에 대해 ChannelUserRelation 생성
      List<ChannelUserRelation> channelUserRelationList = new ArrayList<>();
      usersToAdd.forEach(user -> {
        ChannelUserRelation newRelation = ChannelUserRelation.builder()
            .channel(channel)
            .user(user)
            .readMessage(true)
            .writeMessage(true)
            .viewHistory(true)
            .build();
        channelUserRelationList.add(newRelation);
      });
      channelUserRelationRepository.saveAll(channelUserRelationList);
    }

    Long categoryId = requestDto.getCategoryId();
    Category category = null;
    if (categoryId != null) {
      category = categoryRepository.findByIdAndLogicDeleteFalse(categoryId)
          .orElseThrow(() -> new CategoryException(CATEGORY_NOT_FOUND));
    }

    channel.setting(requestDto, category);
    channelRepository.save(channel);

    // 채널 설정변경 메시지 발송
    ChannelSettingResponseDto channelSettingResponseDto = ChannelSettingResponseDto.builder()
        .id(channelId)
        .name(requestDto.getName())
        .displayOrder(requestDto.getDisplayOrder())
        .open(open)
        .categoryId(categoryId)
        .build();
    String serverUrl = SUB_SERVER + serverId;
    MessageDto newMessageDto = MessageDto.builder()
        .messageType(MessageType.CHANNEL_UPDATE)
        .serverId(serverId)
        .channelId(channelId)
        .message(mapper.writeValueAsString(channelSettingResponseDto))
        .build();
    messagingTemplate.convertAndSend(serverUrl, newMessageDto);
  }

  @Transactional
  public void delete(Long serverId, Long channelId) {
    Channel channel = channelRepository.findByIdAndLogicDeleteFalseAndServerId(channelId, serverId)
        .orElseThrow(() -> new ChannelException(CHANNEL_NOT_FOUND));

    channel.logicDelete();
    channelRepository.save(channel);

    // ChannelUserRelation 모두 삭제
    channelUserRelationQueryRepository.bulkDeleteByChannelId(channelId);

    // ChannelServerRoleRelation 모두 삭제
    channelServerRoleRelationRepository.bulkDeleteByChannelId(channelId);

    // 채널삭제 메시지 발송
    String serverUrl = SUB_SERVER + serverId;
    MessageDto newMessageDto = MessageDto.builder()
        .messageType(MessageType.CHANNEL_DELETE)
        .serverId(serverId)
        .channelId(channelId)
        .build();
    messagingTemplate.convertAndSend(serverUrl, newMessageDto);
  }

  @Transactional
  public void read(Long channelId, Long chatId) {
    String email = customUserDetailsService.getEmailByUserDetails();
    ChannelUserRelationInfoDto channelUserRelationInfoDto = channelUserRelationQueryRepository
        .fetchChannelUserRelationInfoDtoByServerIdAndChannelIdAndUserEmail
            (null, channelId, email);
    User user = channelUserRelationInfoDto.getUser();
    Channel channel = channelUserRelationInfoDto.getChannel();
    ChannelUserRelation channelUserRelation = channelUserRelationInfoDto.getChannelUserRelation();
    if (channel == null) {
      throw new ChannelException(CHANNEL_NOT_FOUND);
    }
    if (channelUserRelation == null) {
      throw new ChannelException(CHANNEL_NOT_PARTICIPATED);
    }
    if (user == null) {
      throw new UserException(USER_UNREGISTERED);
    }
    if (chatRepository.findByIdAndChannelAndLogicDeleteFalse(chatId, channel).isEmpty()) {
      throw new ChatException(CHAT_NOT_FOUND);
    }

    channelUserRelation.updateLastReadMessageId(chatId);
    channelUserRelationRepository.save(channelUserRelation);

    // dm channel인 경우, notification read처리
    if (channel.isDirectMessageChannel()) {
      // 채널, 유저에 해당하는 notification을 모두 찾아서 read 처리
      List<Notification> notificationList = notificationRepository.findByChannelAndMentionedUserAndIsReadFalse(
          channel, user);

      notificationList.forEach(Notification::read);
      notificationRepository.saveAll(notificationList);
    }
  }
}
