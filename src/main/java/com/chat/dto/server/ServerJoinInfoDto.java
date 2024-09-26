package com.chat.dto.server;

import com.chat.domain.channel.Channel;
import com.chat.domain.server.Server;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerJoinInfoDto {

  private Server server;

  private Channel channel;

  private Long channelId;

  @QueryProjection
  @Builder
  public ServerJoinInfoDto(Server server, Channel channel, Long channelId) {
    this.server = server;
    this.channel = channel;
    this.channelId = channelId;
  }
}
