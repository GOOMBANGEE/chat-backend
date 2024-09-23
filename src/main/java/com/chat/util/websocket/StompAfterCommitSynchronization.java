package com.chat.util.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.support.TransactionSynchronization;

@Slf4j
public class StompAfterCommitSynchronization implements TransactionSynchronization {

  private final SimpMessagingTemplate messagingTemplate;
  private final String destinationUrl;
  private final Object payload;

  public StompAfterCommitSynchronization(SimpMessagingTemplate messagingTemplate,
      String destinationUrl, Object payload) {
    this.messagingTemplate = messagingTemplate;
    this.destinationUrl = destinationUrl;
    this.payload = payload;
  }

  @Override
  public void afterCommit() {
    log.debug("트랜잭션 커밋 후 STOMP pub 실행: {}", destinationUrl);
    messagingTemplate.convertAndSend(destinationUrl, payload);
  }

  @Override
  public void afterCompletion(int status) {
    if (status == STATUS_COMMITTED) {
      log.debug("트랜잭션이 정상적으로 커밋됨: {}", destinationUrl);
    } else {
      log.warn("트랜잭션이 롤백됨: {}", destinationUrl);
    }
  }
}