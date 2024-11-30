package com.chat.util.websocket;

import com.chat.domain.channel.ChannelUserRelation;
import com.chat.dto.MessageQueueInitializeDto;
import com.chat.repository.channel.ChannelQueryRepository;
import com.chat.repository.channel.ChannelUserRelationQueryRepository;
import com.chat.repository.channel.ChannelUserRelationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimpleBrokerChannelActivator {

  private final ChannelQueryRepository channelQueryRepository;
  private final ChannelUserRelationRepository channelUserRelationRepository;
  private final ChannelUserRelationQueryRepository channelUserRelationQueryRepository;

  private final SimpMessagingTemplate messagingTemplate;
  private static final String SUB_SERVER = "/sub/server/";
  private static final String SUB_CHANNEL = "/sub/channel/";

  @Transactional
  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationEvent() {
    log.info("서버재시작 -> pub ALL SERVER dummy message");
    log.info("채널재시작 -> pub ALL CHANNEL dummy message");

    List<MessageQueueInitializeDto> messageQueueInitializeDtoList = channelQueryRepository.fetchMessageQueueInitializeDtoList();

    List<ChannelUserRelation> channelUserRelationList = channelUserRelationRepository.findBySubscribeTrue();
    if (channelUserRelationList.isEmpty()) {
      return;
    }
    channelUserRelationQueryRepository.bulkUpdateUnsubscribe(channelUserRelationList);

    messageQueueInitializeDtoList.forEach(dto -> {
      Long serverId = dto.getServerId();
      String serverUrl = SUB_SERVER + serverId;

      Long channelId = dto.getChannelId();
      String channelUrl = SUB_CHANNEL + channelId;

      messagingTemplate.convertAndSend(serverUrl, "dummy message");
      messagingTemplate.convertAndSend(channelUrl, "dummy message");
    });
  }
}