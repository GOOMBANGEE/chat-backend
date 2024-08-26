package com.chat.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageDto {

  public enum MessageType {
    ENTER, SEND, INFO, LEAVE, DELETE
  }

  private MessageType messageType;

  private Long serverId;

  private Long chatId;

  private String username;

  private String message;

  @Builder
  public MessageDto(MessageType messageType, Long serverId, Long chatId, String username,
      String message) {
    this.messageType = messageType;
    this.serverId = serverId;
    this.chatId = chatId;
    this.username = username;
    this.message = message;
  }
}
