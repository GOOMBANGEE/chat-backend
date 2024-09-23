package com.chat.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageQueueInitializeDto {

  private Long serverId;

  private Long channelId;

  @QueryProjection
  @Builder
  public MessageQueueInitializeDto(Long serverId, Long channelId) {
    this.serverId = serverId;
    this.channelId = channelId;
  }
}
