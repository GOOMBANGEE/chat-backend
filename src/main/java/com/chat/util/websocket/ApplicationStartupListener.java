package com.chat.util.websocket;

import com.chat.dto.MessageQueueInitializeDto;
import com.chat.repository.channel.ChannelRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationStartupListener {

  private final ChannelRepository channelRepository;

  private final SimpMessagingTemplate messagingTemplate;
  private static final String SUB_SERVER = "/sub/server/";
  private static final String SUB_CHANNEL = "/sub/channel/";

  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationEvent() {
    log.info("서버재시작 -> pub ALL SERVER dummy message");
    log.info("채널재시작 -> pub ALL CHANNEL dummy message");

    List<MessageQueueInitializeDto> messageQueueInitializeDtoList = channelRepository.fetchMessageQueueInitializeDtoList();

    messageQueueInitializeDtoList.forEach(dto -> {
      Long serverId = dto.getServerId();
      String serverUrl = SUB_SERVER + serverId;

      Long channelId = dto.getChannelId();
      String channelUrl = SUB_CHANNEL + serverId + "/" + channelId;

      messagingTemplate.convertAndSend(serverUrl, "dummy message");
      messagingTemplate.convertAndSend(channelUrl, "dummy message");
    });
  }
}