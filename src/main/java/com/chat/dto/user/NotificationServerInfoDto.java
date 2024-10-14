package com.chat.dto.user;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationServerInfoDto {

  private Long serverId;
  private String serverName;

  private Long channelId;
  private String channelName;

  private Long chatId;
  private String chatMessage;
  private String chatAttachment;
  private LocalDateTime chatCreateTime;
  private LocalDateTime chatUpdateTime;

  private Long userId;
  private String username;
  private String avatarImageSmall;

  @QueryProjection
  @Builder
  public NotificationServerInfoDto(Long serverId, String serverName,
      Long channelId, String channelName,
      Long chatId, String chatMessage, String chatAttachment,
      LocalDateTime chatCreateTime, LocalDateTime chatUpdateTime,
      Long userId, String username, String avatarImageSmall) {
    this.serverId = serverId;
    this.serverName = serverName;
    this.channelId = channelId;
    this.channelName = channelName;
    this.chatId = chatId;
    this.chatMessage = chatMessage;
    this.chatAttachment = chatAttachment;
    this.chatCreateTime = chatCreateTime;
    this.chatUpdateTime = chatUpdateTime;
    this.userId = userId;
    this.username = username;
    this.avatarImageSmall = avatarImageSmall;
  }
}
