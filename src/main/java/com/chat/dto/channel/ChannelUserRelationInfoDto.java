package com.chat.dto.channel;

import com.chat.domain.channel.Channel;
import com.chat.domain.channel.ChannelUserRelation;
import com.chat.domain.server.Server;
import com.chat.domain.user.User;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChannelUserRelationInfoDto {

  private Server server;
  private Channel channel;
  private ChannelUserRelation channelUserRelation;
  private User user;

  @QueryProjection
  @Builder
  public ChannelUserRelationInfoDto(Server server, Channel channel,
      ChannelUserRelation channelUserRelation, User user) {
    this.server = server;
    this.channel = channel;
    this.channelUserRelation = channelUserRelation;
    this.user = user;
  }
}
