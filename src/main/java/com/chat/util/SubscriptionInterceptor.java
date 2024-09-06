package com.chat.util;


import com.chat.exception.ServerException;
import com.chat.jwt.TokenProvider;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscriptionInterceptor implements ChannelInterceptor {

  private final TokenProvider tokenProvider;

  private static final String USER_UNREGISTERED = "SERVER:USER_UNREGISTERED";
  private static final String INVALID_PATH = "SERVER:INVALID_PATH";
  private static final String INVALID_TOKEN = "SERVER:INVALID_TOKEN";
  private static final String BEARER_PREFIX = "Bearer ";

  private static final String USER = "user";
  private static final String SERVER = "server";

  private static final String AUTHORIZATION = "Authorization";

  // 이전 구독방법
  // 구독하는 경로 /sub/server/{serverId} /sub/user/{userId}
  // 클라이언트에서 각 serverId 별 구독요청 보냄
  // 해당 방법으로 인증만 추가할 경우 쿼리 다수 발생

  // 구독방식 개선
  // sub server -> 기존의 sub server, user통합해서 구독시킴
  // /sub/server/로 들어오는 요청에서 header Authorization 확인
  // token payload -> subServer list -> serverId 확인

  // 유저가 서버 생성, 참가, 퇴장시 refreshToken 갱신필요
  @Override
  public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor == null) {
      throw new ServerException(INVALID_PATH);
    }
    if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
      // /sub/server/{serverId}
      String destination = accessor.getDestination();
      if (destination == null) {
        throw new ServerException(INVALID_PATH);
      }

      String type = destination.split("/")[2];
      if (type.equals(USER)) {
        validateUserSubscription(accessor, destination);
      } else if (type.equals(SERVER)) {
        validateServerSubscription(accessor, destination);
      } else {
        throw new ServerException(INVALID_PATH);
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

  private Long extractPathId(String destination) {
    String pathString = destination.split("/")[3]; // serverId
    long path;
    try {
      path = Long.parseLong(pathString);
    } catch (NumberFormatException e) {
      throw new ServerException(INVALID_PATH);
    }
    return path;
  }

  private void validateUserSubscription(StompHeaderAccessor accessor, String destination) {
    Long userId = extractPathId(destination);
    // user에 해당하는 userId로 /sub/user/{userId} 구독
    String accessToken = this.extractAccessToken(accessor);
    Long userIdFromToken = tokenProvider.getUserIdFromToken(accessToken);
    if (!userIdFromToken.equals(userId)) {
      throw new ServerException(USER_UNREGISTERED);
    }
  }

  private void validateServerSubscription(StompHeaderAccessor accessor, String destination) {
    Long serverId = extractPathId(destination);
    // 토큰 subServer list 에 있다면 구독
    String accessToken = this.extractAccessToken(accessor);
    List<Long> subServerList = tokenProvider.getSubServerFromToken(accessToken);
    if (!subServerList.contains(serverId)) {
      throw new ServerException(USER_UNREGISTERED);
    }
  }
}