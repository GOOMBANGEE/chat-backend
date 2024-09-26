package com.chat.dto.server;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerJoinResponseDto {

  private Long id;

  private String name;

  private Long channelId;

  @Builder
  public ServerJoinResponseDto(Long id, String name, Long channelId) {
    this.id = id;
    this.name = name;
    this.channelId = channelId;
  }
}
