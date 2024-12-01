package com.chat.dto.user;

import com.chat.domain.channel.ChannelUserRelation;
import com.chat.domain.user.User;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeoutDto {

  private User user;

  private Long userId;

  private Long serverId;

  private ChannelUserRelation channelUserRelation;

  @QueryProjection
  @Builder
  public TimeoutDto(User user, Long userId,
      Long serverId, ChannelUserRelation channelUserRelation) {
    this.user = user;
    this.userId = userId;
    this.serverId = serverId;
    this.channelUserRelation = channelUserRelation;
  }
}
