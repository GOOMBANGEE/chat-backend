package com.chat.service.server;

import com.chat.domain.server.Server;
import com.chat.domain.server.ServerUserRelation;
import com.chat.domain.user.User;
import com.chat.dto.MessageDto;
import com.chat.dto.MessageDto.MessageType;
import com.chat.dto.server.ServerCreateRequestDto;
import com.chat.dto.server.ServerCreateResponseDto;
import com.chat.dto.server.ServerDeleteRequestDto;
import com.chat.dto.server.ServerInfoDto;
import com.chat.dto.server.ServerInviteInfoResponseDto;
import com.chat.dto.server.ServerInviteResponseDto;
import com.chat.dto.server.ServerJoinResponseDto;
import com.chat.dto.server.ServerListResponseDto;
import com.chat.dto.server.ServerSettingRequestDto;
import com.chat.exception.ServerException;
import com.chat.repository.server.ServerRepository;
import com.chat.repository.server.ServerUserRelationRepository;
import com.chat.repository.user.UserRepository;
import com.chat.service.user.CustomUserDetailsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.SecureRandom;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ServerService {

  private final CustomUserDetailsService customUserDetailsService;
  private final ServerRepository serverRepository;
  private final UserRepository userRepository;
  private final ServerUserRelationRepository serverUserRelationRepository;

  private static final String USER_UNREGISTERED = "SERVER:USER_UNREGISTERED";
  private static final String SERVER_NOT_FOUND = "SERVER:SERVER_NOT_FOUND";
  private static final String SERVER_NAME_INVALID = "SERVER:SERVER_NAME_INVALID";
  private static final String SERVER_NOT_PARTICIPATED = "SERVER:SERVER_NOT_PARTICIPATED";
  private static final String SERVER_NOT_PERMITTED = "SERVER:SERVER_NOT_PERMITTED";
  private static final String SERVER_ALREADY_JOINED = "SERVER:SERVER_ALREADY_JOINED";

  private final SimpMessagingTemplate messagingTemplate;
  private static final String SUB_SERVER = "/sub/server/";
  private final ObjectMapper mapper = new ObjectMapper();

  @Value("${server.front-url}")
  private String frontUrl;

  // 서버 생성
  @Transactional
  public ServerCreateResponseDto create(ServerCreateRequestDto requestDto) {
    // 등록된 유저인지 확인
    String email = customUserDetailsService.getEmailByUserDetails();
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new ServerException(USER_UNREGISTERED));

    // 서버 생성
    String username = requestDto.getUsername();
    String name = requestDto.getName();

    Server server = Server.builder()
        .name(name)
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

    Long id = server.getServerIdForServerCreateResponse();
    return ServerCreateResponseDto.builder()
        .id(id)
        .name(name)
        .build();
  }

  // 참여중인 서버 목록
  public ServerListResponseDto list() {
    // 해당 유저가 속한 서버 리턴
    String email = customUserDetailsService.getEmailByUserDetails();
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new ServerException(USER_UNREGISTERED));

    List<ServerInfoDto> serverInfoDtoList = serverUserRelationRepository.fetchServerInfoDtoListByUser(
        user);

    return ServerListResponseDto.builder()
        .serverList(serverInfoDtoList)
        .build();
  }

  // 서버 설정변경
  @Transactional
  public void setting(Long serverId, ServerSettingRequestDto requestDto)
      throws JsonProcessingException {
    String email = customUserDetailsService.getEmailByUserDetails();

    // 해당 서버 참여자인지 확인
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new ServerException(USER_UNREGISTERED));

    Server server = serverRepository.findByIdAndLogicDeleteFalse(serverId)
        .orElseThrow(() -> new ServerException(SERVER_NOT_FOUND));

    // 서버의 주인인지 확인
    ServerUserRelation serverUserRelation = serverUserRelationRepository.findServerUserRelationByUserAndServer(
            user, server)
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
          .messageType(MessageType.INFO)
          .serverId(serverId)
          .message(mapper.writeValueAsString(serverInfoDto))
          .build();
      messagingTemplate.convertAndSend(serverUrl, newMessageDto);
      return;
    }
    throw new ServerException(SERVER_NOT_PERMITTED);
  }


  // 서버 입장
  @Transactional
  public ServerJoinResponseDto join(String code) {
    String email = customUserDetailsService.getEmailByUserDetails();

    // 해당 서버 참여자인지 확인
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new ServerException(USER_UNREGISTERED));

    Server server = serverRepository.findByCodeAndLogicDeleteFalse(code)
        .orElseThrow(() -> new ServerException(SERVER_NOT_FOUND));

    boolean isAlreadyJoined = serverUserRelationRepository.findServerUserRelationByUserAndServer(
        user, server).isPresent();
    if (isAlreadyJoined) {
      throw new ServerException(SERVER_ALREADY_JOINED);
    }

    // 서버 입장 저장
    ServerUserRelation serverUserRelation = ServerUserRelation.builder()
        .server(server)
        .user(user)
        .build();
    serverUserRelationRepository.save(serverUserRelation);

    // return 입장한 서버 id
    Long serverId = server.getServerIdForServerJoinResponse();
    return ServerJoinResponseDto.builder()
        .serverId(serverId)
        .build();
  }

  // 서버 초대코드 조회
  public ServerInviteInfoResponseDto inviteInfo(String code) {
    String email = customUserDetailsService.getEmailByUserDetails();

    // 해당 서버 참여자인지 확인
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new ServerException(USER_UNREGISTERED));

    Server server = serverRepository.findByCodeAndLogicDeleteFalse(code)
        .orElseThrow(() -> new ServerException(SERVER_NOT_FOUND));

    boolean isAlreadyJoined = serverUserRelationRepository.findServerUserRelationByUserAndServer(
        user, server).isPresent();
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
        .orElseThrow(() -> new ServerException(USER_UNREGISTERED));

    // todo role check
    // 현재는 참여자확인만 이루어짐
    Server server = serverUserRelationRepository.findServerByUserAndServerId(user, serverId)
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

  // 서버 삭제
  @Transactional
  public void delete(Long serverId, ServerDeleteRequestDto requestDto) {
    String email = customUserDetailsService.getEmailByUserDetails();

    // 해당 서버 참여자인지 확인
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new ServerException(USER_UNREGISTERED));

    Server server = serverRepository.findByIdAndLogicDeleteFalse(serverId)
        .orElseThrow(() -> new ServerException(SERVER_NOT_FOUND));

    String name = requestDto.getName();
    if (!server.checkName(name)) {
      throw new ServerException(SERVER_NAME_INVALID);
    }

    // 서버의 주인인지 확인
    ServerUserRelation serverUserRelation = serverUserRelationRepository.findServerUserRelationByUserAndServer(
            user, server)
        .orElseThrow(() -> new ServerException(SERVER_NOT_PARTICIPATED));

    // 주인이 아닌경우 권한없음
    if (serverUserRelation.isOwner()) {
      server.logicDelete();
      serverRepository.save(server);

      // stomp pub
      String serverUrl = SUB_SERVER + serverId;
      MessageDto newMessageDto = MessageDto.builder()
          .messageType(MessageType.DELETE_SERVER)
          .serverId(serverId)
          .build();
      messagingTemplate.convertAndSend(serverUrl, newMessageDto);
      return;
    }
    throw new ServerException(SERVER_NOT_PERMITTED);
  }

}
