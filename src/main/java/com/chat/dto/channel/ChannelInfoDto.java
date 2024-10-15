package com.chat.dto.channel;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChannelInfoDto {

  private Long id;

  private String name;

  private Double displayOrder;

  private Long serverId;

  private Long categoryId;

  private Long lastReadMessageId;

  private Long lastMessageId;

  private Long userDirectMessageId;
  private String username;
  private String avatarImageSmall;

  @QueryProjection
  @Builder
  public ChannelInfoDto(Long id, String name, Double displayOrder, Long serverId, Long categoryId,
      Long lastReadMessageId, Long lastMessageId) {
    this.id = id;
    this.name = name;
    this.displayOrder = displayOrder;
    this.serverId = serverId;
    this.categoryId = categoryId;
    this.lastReadMessageId = lastReadMessageId;
    this.lastMessageId = lastMessageId;
  }

  @QueryProjection
  @Builder
  public ChannelInfoDto(Long id, String name, Double displayOrder, Long serverId, Long categoryId,
      Long lastReadMessageId, Long lastMessageId,
      Long userDirectMessageId, String username, String avatarImageSmall) {
    this.id = id;
    this.name = name;
    this.displayOrder = displayOrder;
    this.serverId = serverId;
    this.categoryId = categoryId;
    this.lastReadMessageId = lastReadMessageId;
    this.lastMessageId = lastMessageId;
    this.userDirectMessageId = userDirectMessageId;
    this.username = username;
    this.avatarImageSmall = avatarImageSmall;
  }
}
