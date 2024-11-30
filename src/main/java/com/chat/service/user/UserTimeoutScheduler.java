package com.chat.service.user;

import static java.util.stream.Collectors.toList;

import com.chat.domain.channel.ChannelUserRelation;
import com.chat.dto.MessageDto;
import com.chat.dto.MessageDto.MessageType;
import com.chat.dto.user.UserAndServerAndChannelUserRelationForTimeoutCheckDto;
import com.chat.repository.channel.ChannelUserRelationQueryRepository;
import com.chat.repository.user.UserRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserTimeoutScheduler {

  private final UserRepository userRepository;
  private final ChannelUserRelationQueryRepository channelUserRelationQueryRepository;
  private final SimpMessagingTemplate messagingTemplate;

  private static final Duration TIMEOUT = Duration.ofMinutes(30);
  private static final String SUB_SERVER = "/sub/server/";

  @Value("${server.time-zone}")
  private String timeZone;

  // 1분 마다 타임아웃된 유저를 체크
  @Scheduled(fixedRate = 60 * 1000)
  @Transactional
  public void checkUserTimeouts() {
    // lastlogin이 TIMEOUT시간 넘어간 유저 찾기
    LocalDateTime now = LocalDateTime.now(ZoneId.of(timeZone));
    LocalDateTime timeout = now.minus(TIMEOUT);

    List<UserAndServerAndChannelUserRelationForTimeoutCheckDto> timeoutDtoList = channelUserRelationQueryRepository
        .fetchUserAndServerAndChannelUserRelationForTimeoutCheckDto(timeout);

    // 해당되는 항목 없는경우 쿼리가 나가지않도록 설정
    if (timeoutDtoList.isEmpty()) {
      return;
    }

    // batch update
    List<Long> userIdList = timeoutDtoList.stream()
        .map(UserAndServerAndChannelUserRelationForTimeoutCheckDto::getUserId)
        .distinct()
        .toList();
    List<ChannelUserRelation> channelUserRelationList = timeoutDtoList.stream()
        .map(UserAndServerAndChannelUserRelationForTimeoutCheckDto::getChannelUserRelation)
        .toList();
    userRepository.bulkUpdateOffline(userIdList);
    channelUserRelationQueryRepository.bulkUpdateUnsubscribe(channelUserRelationList);

    // 유저 오프라인 메시지 pub
    // 유저id를 기준으로 서버id리스트로 묶어줌
    Map<Long, List<Long>> userServerMap = timeoutDtoList.stream()
        .collect(
            Collectors.groupingBy(UserAndServerAndChannelUserRelationForTimeoutCheckDto::getUserId,
                Collectors.mapping(
                    UserAndServerAndChannelUserRelationForTimeoutCheckDto::getServerId,
                    toList())));
    userServerMap.forEach((userId, serverIdList) ->
        serverIdList.forEach(serverId ->
            CompletableFuture.runAsync(() ->
                sendOfflineMessage(userId, serverId))));
  }

  private void sendOfflineMessage(Long userId, Long serverId) {
    String serverUrl = SUB_SERVER + serverId;
    MessageDto messageDto = MessageDto.builder()
        .messageType(MessageType.USER_OFFLINE)
        .serverId(serverId)
        .userId(userId)
        .build();
    messagingTemplate.convertAndSend(serverUrl, messageDto);
  }
}
