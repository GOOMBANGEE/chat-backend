package com.chat.util.websocket;


import com.chat.exception.ChannelException;
import com.chat.exception.ServerException;
import com.chat.exception.UserException;
import com.chat.jwt.TokenProvider;
import com.chat.repository.channel.ChannelRepository;
import com.chat.repository.channel.ChannelUserRelationRepository;
import com.chat.repository.server.ServerRepository;
import com.chat.repository.server.ServerUserRelationRepository;
import com.chat.repository.user.UserRepository;
import java.util.Objects;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionInterceptor implements ChannelInterceptor {

  private final ServerRepository serverRepository;
  private final ServerUserRelationRepository serverUserRelationRepository;
  private final ChannelRepository channelRepository;
  private final ChannelUserRelationRepository channelUserRelationRepository;
  private final UserRepository userRepository;

  private final TokenProvider tokenProvider;

  private static final String USER_UNREGISTERED = "USER:USER_UNREGISTERED";
  private static final String INVALID_PATH = "SERVER:INVALID_PATH";
  private static final String INVALID_TOKEN = "SERVER:INVALID_TOKEN";
  private static final String SERVER_NOT_PARTICIPATED = "SERVER:SERVER_NOT_PARTICIPATED";
  private static final String CHANNEL_NOT_PARTICIPATED = "CHANNEL:CHANNEL_NOT_PARTICIPATED";

  private static final String AUTHORIZATION = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String USER = "user";
  private static final String SERVER = "server";
  private static final String CHANNEL = "channel";

  // 이전 구독방법
  // 구독하는 경로 /sub/server/{serverId} /sub/user/{userId}
  // 클라이언트에서 각 serverId 별 구독요청 보냄
  // 해당 방법으로 인증만 추가할 경우 쿼리 다수 발생

  // 이전 구독방법 2
  // sub server -> 기존의 sub server, user통합해서 구독시킴
  // /sub/server/로 들어오는 요청에서 header Authorization 확인
  // token payload -> subServer list -> serverId 확인

  // 구독방식 개선
  // 기존 token payload에서 검사하는방식
  // sub/user/{userId} -> user updateOnline -> 해당 유저가 속해있는 서버, 채널에 접속표시 + userId 구독활성화
  // sub/server/{serverId} -> user online, user resetUsername 알림받기용도
  // sub/channel/{serverId}/{channelId} -> token에서 user정보 가져옴 -> db조회
  // 처음 구독방법과 같은방법에 유저인증만 추가 -> 접속한 채널만 추가로 구독하는 방식으로 접속시마다 발생하는 다수쿼리 발생을 줄임

  @Override
  public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor == null) {
      throw new ServerException(INVALID_PATH);
    }
    SimpMessageType messageType = accessor.getMessageType();
    if (Objects.requireNonNull(messageType) == SimpMessageType.SUBSCRIBE) {
      // sub/user/{userId}
      // sub/server/{serverId}
      // sub/channel/{channelId}
      String destination = accessor.getDestination();
      if (destination == null) {
        throw new ServerException(INVALID_PATH);
      }

      String type = destination.split("/")[2];
      switch (type) {
        case USER -> validateUserSubscription(accessor, destination);
        case SERVER -> validateServerSubscription(accessor, destination);
        case CHANNEL -> validateChannelSubscription(accessor, destination);
        default -> throw new ServerException(INVALID_PATH);
      }
    }
    return message;
  }

  private String extractAccessToken(StompHeaderAccessor accessor) {
    String accessToken = accessor.getFirstNativeHeader(AUTHORIZATION);
    if (accessToken == null || !accessToken.startsWith(BEARER_PREFIX)) {
      throw new ServerException(INVALID_TOKEN);
    }
    return accessToken.substring(BEARER_PREFIX.length());
  }

  private Long extractPathId(String destination, int splitCount) {
    String pathString = destination.split("/")[splitCount]; // userId, channelId
    long path;
    try {
      path = Long.parseLong(pathString);
    } catch (NumberFormatException e) {
      throw new ServerException(INVALID_PATH);
    }
    return path;
  }

  private void validateUserSubscription(StompHeaderAccessor accessor, String destination) {
    Long userId = extractPathId(destination, 3);
    // user에 해당하는 userId로 /sub/user/{userId} 구독
    String accessToken = this.extractAccessToken(accessor);
    Long userIdFromToken = tokenProvider.getUserIdFromToken(accessToken);
    if (!userIdFromToken.equals(userId)) {
      throw new UserException(USER_UNREGISTERED);
    }
  }

  private void validateServerSubscription(StompHeaderAccessor accessor, String destination) {
    Long serverId = extractPathId(destination, 3);
    // server에 참가중인지 확인
    String accessToken = this.extractAccessToken(accessor);
    Long userIdFromToken = tokenProvider.getUserIdFromToken(accessToken);

    if (serverUserRelationRepository
        .fetchServerUserRelationByServerIdAndUserId(serverId, userIdFromToken)
        .isEmpty()) {
      throw new ServerException(SERVER_NOT_PARTICIPATED);
    }
  }

  private void validateChannelSubscription(StompHeaderAccessor accessor, String destination) {
    // token userId, path channelId를 가져와서 channel에 속해있는지 확인
    String accessToken = this.extractAccessToken(accessor);
    Long userIdFromToken = tokenProvider.getUserIdFromToken(accessToken);
    Long channelId = extractPathId(destination, 3);

    // 기존방식 -> 특정유저, 특정역할에 대해서 검색
    // 개선방안 -> ChannelUserRelation에 모든 채널-유저 정보가 담겨있어서 해당 엔티티가 존재하는지 검색
    if (channelUserRelationRepository
        .fetchChannelUserRelationByChannelIdAndUserId(channelId, userIdFromToken)
        .isEmpty()) {
      throw new ChannelException(CHANNEL_NOT_PARTICIPATED);
    }
  }
}