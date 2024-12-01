package com.chat.dto.channel;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChannelRegistrationDto {

  private Long channel;

  private Long lastMessageId;

  @QueryProjection
  @Builder
  public ChannelRegistrationDto(Long channel, Long lastMessageId) {
    this.channel = channel;
    this.lastMessageId = lastMessageId;
  }
}
