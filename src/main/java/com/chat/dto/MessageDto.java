package com.chat.dto;

import com.chat.dto.chat.ChatInfoDto;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageDto {

  public enum MessageType {
    SERVER_CREATE, SERVER_ENTER, SERVER_UPDATE_NAME, SERVER_UPDATE_ICON, SERVER_DELETE, SERVER_LEAVE,
    CATEGORY_CREATE, CATEGORY_UPDATE, CATEGORY_DELETE,
    CHANNEL_CREATE, CHANNEL_UPDATE, CHANNEL_DELETE,
    CHANNEL_CREATE_DIRECT_MESSAGE,
    CHAT_SEND, CHAT_UPDATE, CHAT_DELETE,
    USER_ONLINE, USER_OFFLINE, USER_UPDATE_USERNAME, USER_UPDATE_AVATAR,
    FRIEND_REQUEST, FRIEND_ACCEPT, FRIEND_DELETE
  }

  private MessageType messageType;

  private Long serverId;
  private Long categoryId;
  private Long channelId;
  private Long chatId;

  private Long userId;
  private String username;
  private String avatar;

  private String message;
  private String attachmentType;
  private String attachment;
  private Integer attachmentWidth;
  private Integer attachmentHeight;

  // 답장 들어올때
  private Long chatReference;
  private boolean chatReferenceNotification;
  // 답장 내보낼때
  private ChatInfoDto chatReferenceInfo;

  private LocalDateTime createTime;
  private LocalDateTime updateTime;

  @Builder
  public MessageDto(MessageType messageType, Long serverId, Long categoryId, Long channelId,
      Long chatId, Long userId, String username, String avatar, String message,
      String attachmentType, String attachment, Integer attachmentWidth, Integer attachmentHeight,
      Long chatReference, boolean chatReferenceNotification,
      ChatInfoDto chatReferenceInfo, LocalDateTime createTime, LocalDateTime updateTime) {
    this.messageType = messageType;
    this.serverId = serverId;
    this.categoryId = categoryId;
    this.channelId = channelId;
    this.chatId = chatId;
    this.userId = userId;
    this.username = username;
    this.avatar = avatar;
    this.message = message;
    this.attachmentType = attachmentType;
    this.attachment = attachment;
    this.attachmentWidth = attachmentWidth;
    this.attachmentHeight = attachmentHeight;
    this.chatReference = chatReference;
    this.chatReferenceNotification = chatReferenceNotification;
    this.chatReferenceInfo = chatReferenceInfo;
    this.createTime = createTime;
    this.updateTime = updateTime;
  }
}
