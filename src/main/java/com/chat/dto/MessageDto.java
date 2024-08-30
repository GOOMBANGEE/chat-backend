package com.chat.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageDto {

  public enum MessageType {
    ENTER, SEND, INFO, LEAVE,
    UPDATE_CHAT, DELETE_CHAT,
    DELETE_SERVER
  }

  private MessageType messageType;

  private Long serverId;

  private Long chatId;

  private Long userId;

  private String username;

  private String message;

  private boolean enter;

  private boolean leave;

  @Builder
  public MessageDto(MessageType messageType, Long serverId, Long chatId, Long userId,
      String username,
      String message, boolean enter, boolean leave) {
    this.messageType = messageType;
    this.serverId = serverId;
    this.chatId = chatId;
    this.userId = userId;
    this.username = username;
    this.message = message;
    this.enter = enter;
    this.leave = leave;
  }
}
