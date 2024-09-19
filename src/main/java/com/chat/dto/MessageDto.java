package com.chat.dto;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageDto {

  public enum MessageType {
    ENTER, SEND, INFO, LEAVE,
    DELETE_SERVER,
    CREATE_CHANNEL, UPDATE_CHANNEL, DELETE_CHANNEL,
    UPDATE_CHAT, DELETE_CHAT,
    DELETE_SERVER
  }

  private MessageType messageType;

  private Long serverId;

  private Long categoryId;

  private Long channelId;

  private Long chatId;

  private Long userId;

  private String username;

  private String message;

  private boolean enter;

  private boolean leave;

  private boolean friendRequest;

  private boolean friendAccept;

  private boolean friendDelete;

  private LocalDateTime createTime;

  private LocalDateTime updateTime;

  @Builder
  public MessageDto(MessageType messageType, Long serverId, Long categoryId, Long channelId,
      Long chatId, Long userId, String username, String message, boolean enter, boolean leave,
      boolean friendRequest, boolean friendAccept, boolean friendDelete, LocalDateTime createTime,
      LocalDateTime updateTime) {
    this.messageType = messageType;
    this.serverId = serverId;
    this.categoryId = categoryId;
    this.channelId = channelId;
    this.chatId = chatId;
    this.userId = userId;
    this.username = username;
    this.message = message;
    this.enter = enter;
    this.leave = leave;
    this.friendRequest = friendRequest;
    this.friendAccept = friendAccept;
    this.friendDelete = friendDelete;
    this.createTime = createTime;
    this.updateTime = updateTime;
  }
}
