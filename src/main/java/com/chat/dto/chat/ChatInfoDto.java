package com.chat.dto.chat;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatInfoDto {

  private Long id;

  private Long userId;
  private String username;
  private String avatarImageSmall;

  private String message;
  private String attachmentType;
  private String attachment;
  private Integer attachmentWidth;
  private Integer attachmentHeight;

  private boolean enter;

  private LocalDateTime createTime;
  private LocalDateTime updateTime;

  private Long referenceId;
  private String referenceUsername;
  private String referenceAvatarImageSmall;
  private String referenceMessage;
  private String referenceAttachmentType;

  @QueryProjection
  @Builder
  public ChatInfoDto(
      Long id,
      Long userId, String username, String avatarImageSmall,
      String message,
      String attachmentType, String attachment, Integer attachmentWidth, Integer attachmentHeight,
      boolean enter,
      LocalDateTime createTime, LocalDateTime updateTime,
      Long referenceId, String referenceUsername, String referenceAvatarImageSmall,
      String referenceMessage, String referenceAttachmentType
  ) {
    this.id = id;
    this.userId = userId;
    this.username = username;
    this.avatarImageSmall = avatarImageSmall;
    this.message = message;
    this.attachmentType = attachmentType;
    this.attachment = attachment;
    this.attachmentWidth = attachmentWidth;
    this.attachmentHeight = attachmentHeight;
    this.enter = enter;
    this.createTime = createTime;
    this.updateTime = updateTime;
    this.referenceId = referenceId;
    this.referenceUsername = referenceUsername;
    this.referenceAvatarImageSmall = referenceAvatarImageSmall;
    this.referenceMessage = referenceMessage;
    this.referenceAttachmentType = referenceAttachmentType;
  }
}
