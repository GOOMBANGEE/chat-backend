package com.chat.service.channel;

import com.chat.domain.category.Category;
import com.chat.domain.channel.Channel;
import com.chat.domain.channel.ChannelServerRoleRelation;
import com.chat.domain.channel.ChannelUserRelation;
import com.chat.domain.server.Server;
import com.chat.domain.server.ServerRole;
import com.chat.domain.server.ServerUserRelation;
import com.chat.domain.user.User;
import com.chat.dto.MessageDto;
import com.chat.dto.MessageDto.MessageType;
import com.chat.dto.channel.ChannelCreateRequestDto;
import com.chat.dto.channel.ChannelCreateResponseDto;
import com.chat.dto.channel.ChannelRenameRequestDto;
import com.chat.dto.channel.ChannelSettingRequestDto;
import com.chat.dto.channel.ChannelSettingResponseDto;
import com.chat.exception.CategoryException;
import com.chat.exception.ChannelException;
import com.chat.exception.ServerException;
import com.chat.exception.UserException;
import com.chat.repository.category.CategoryRepository;
import com.chat.repository.channel.ChannelRepository;
import com.chat.repository.channel.ChannelServerRoleRelationRepository;
import com.chat.repository.channel.ChannelUserRelationRepository;
import com.chat.repository.server.ServerRepository;
import com.chat.repository.server.ServerRoleRepository;
import com.chat.repository.server.ServerRoleUserRelationRepository;
import com.chat.repository.server.ServerUserRelationRepository;
import com.chat.repository.user.UserRepository;
import com.chat.service.user.CustomUserDetailsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
  private final UserRepository userRepository;
  private final ServerRepository serverRepository;
  private final ServerRoleRepository serverRoleRepository;
  private final ServerUserRelationRepository serverUserRelationRepository;
  private final ServerRoleUserRelationRepository serverRoleUserRelationRepository;
  private final CategoryRepository categoryRepository;
  private final ChannelRepository channelRepository;
  private final ChannelServerRoleRelationRepository channelServerRoleRelationRepository;
  private final ChannelUserRelationRepository channelUserRelationRepository;

  private static final String USER_UNREGISTERED = "USER:USER_UNREGISTERED";
  private static final String SERVER_NOT_FOUND = "SERVER:SERVER_NOT_FOUND";
  private static final String CATEGORY_NOT_FOUND = "CATEGORY:CATEGORY_NOT_FOUND";
  private static final String SERVER_NOT_PARTICIPATED = "SERVER:SERVER_NOT_PARTICIPATED";
  private static final String NO_CHANNEL_CREATE_PERMISSION = "SERVER:NO_CHANNEL_CREATE_PERMISSION";
  private static final String CHANNEL_NOT_FOUND = "CHANNEL:CHANNEL_NOT_FOUND";

  private final SimpMessagingTemplate messagingTemplate;
  private static final String SUB_SERVER = "/sub/server/";
  private final ObjectMapper mapper = new ObjectMapper();


  @Transactional
  public ChannelCreateResponseDto create(
      Long serverId, ChannelCreateRequestDto requestDto) throws JsonProcessingException {
    // 등록된 유저인지 확인
    String email = customUserDetailsService.getEmailByUserDetails();
    User user = userRepository
        .findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    Server server = serverRepository
        .findByIdAndLogicDeleteFalse(serverId)
        .orElseThrow(() -> new ServerException(SERVER_NOT_FOUND));
    ServerUserRelation serverUserRelation = serverUserRelationRepository
        .findByUserAndServerAndLogicDeleteFalse(user, server)
        .orElseThrow(() -> new ServerException(SERVER_NOT_PARTICIPATED));
    List<ServerRole> serverRoleUserRelation = serverRoleUserRelationRepository
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
    Double displayOrder = categoryRepository.fetchMaxDisplayOrder(category) * 2;
    boolean open = allowRoleIdList == null && allowUserIdList == null;

    Channel channel = Channel.builder()
        .name(name)
        .displayOrder(displayOrder)
        .open(open)
        .logicDelete(false)
        .server(server)
        .category(category)
        .build();
    channelRepository.save(channel);

    if (allowRoleIdList != null) {
      // 여러 역할 한번에 조회
      List<ServerRole> serverRoleList = serverRoleRepository.findByIdInAndLogicDeleteFalse(
          allowRoleIdList);
      // 조회된 역할들 순회하면서 ChannelServerRoleRelation 생성
      List<ChannelServerRoleRelation> relationList = serverRoleList.stream()
          .map(serverRole -> ChannelServerRoleRelation.builder()
              .channel(channel)
              .serverRole(serverRole)
              .readMessage(true)
              .writeMessage(true)
              .viewHistory(true)
              .build())
          .toList();
      // 한번에 저장
      channelServerRoleRelationRepository.saveAll(relationList);
    }

    if (allowUserIdList != null) {
      // 여러 유저 한번에 조회
      List<User> userList = userRepository.findByIdInAndLogicDeleteFalse(allowUserIdList);
      // 조회된 유저들 순회하면서 ChannelUserRelation 생성
      List<ChannelUserRelation> relationList = userList.stream()
          .map(userInList -> ChannelUserRelation.builder()
              .channel(channel)
              .channel(channel)
              .user(userInList)
              .readMessage(true)
              .writeMessage(true)
              .viewHistory(true)
              .build())
          .toList();
      // 한번에 저장
      channelUserRelationRepository.saveAll(relationList);
    }

    Long channelId = channel.getChannelIdForServerCreateResponse();
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
        .messageType(MessageType.INFO)
        .serverId(serverId)
        .message(mapper.writeValueAsString(responseDto))
        .build();
    messagingTemplate.convertAndSend(serverUrl, newMessageDto);
    return responseDto;
  }

  @Transactional
  public void rename(Long serverId, Long channelId, ChannelRenameRequestDto requestDto)
      throws JsonProcessingException {
    Channel channel = channelRepository.findByIdAndLogicDeleteFalseAndServerId(channelId, serverId)
        .orElseThrow(() -> new ChannelException(CHANNEL_NOT_FOUND));
    // todo
    // 권한확인필요

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
        .messageType(MessageType.UPDATE_CHANNEL)
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
      // open 설정시 기존 ChannelServerRoleRelation, ChannelUserRelation 모두 삭제
      List<ChannelServerRoleRelation> channelServerRoleRelationList = channelServerRoleRelationRepository
          .findByChannel(channel);
      List<ChannelUserRelation> channelUserRelationList = channelUserRelationRepository
          .findByChannel(channel);
      channelServerRoleRelationRepository.deleteAll(channelServerRoleRelationList);
      channelUserRelationRepository.deleteAll(channelUserRelationList);
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
        .messageType(MessageType.UPDATE_CHANNEL)
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

    // 채널삭제 메시지 발송
    String serverUrl = SUB_SERVER + serverId;
    MessageDto newMessageDto = MessageDto.builder()
        .messageType(MessageType.DELETE_CHANNEL)
        .serverId(serverId)
        .channelId(channelId)
        .build();
    messagingTemplate.convertAndSend(serverUrl, newMessageDto);
  }
}