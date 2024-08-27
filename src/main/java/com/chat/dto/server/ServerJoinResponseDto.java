package com.chat.dto.server;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerJoinResponseDto {

  private Long serverId;

  @Builder
  public ServerJoinResponseDto(Long serverId) {
    this.serverId = serverId;
  }
}
