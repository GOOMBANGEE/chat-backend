package com.chat.dto.chat;

import com.chat.domain.chat.Chat;
import com.chat.domain.user.User;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatReferenceInfoForSendMessageResponse {

  private Chat chat;

  private User user;

  private Long id;
  private String username;
  private String avatarImageSmall;
  private String message;
  private String attachmentType;

  @QueryProjection
  @Builder
  public ChatReferenceInfoForSendMessageResponse(Chat chat, User user, Long id, String username,
      String avatarImageSmall, String message, String attachmentType) {
    this.chat = chat;
    this.user = user;
    this.id = id;
    this.username = username;
    this.avatarImageSmall = avatarImageSmall;
    this.message = message;
    this.attachmentType = attachmentType;
  }
}
