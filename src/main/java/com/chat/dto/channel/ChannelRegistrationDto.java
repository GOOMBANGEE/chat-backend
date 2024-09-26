package com.chat.dto.channel;

import com.chat.domain.channel.Channel;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChannelRegistrationDto {

  private Channel channel;

  private Long lastMessageId;

  @QueryProjection
  @Builder
  public ChannelRegistrationDto(Channel channel, Long lastMessageId) {
    this.channel = channel;
    this.lastMessageId = lastMessageId;
  }
}
