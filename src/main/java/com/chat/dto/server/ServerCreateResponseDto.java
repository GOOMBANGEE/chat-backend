package com.chat.dto.server;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerCreateResponseDto {

  private Long id;

  @Builder
  public ServerCreateResponseDto(Long id) {
    this.id = id;
  }
}
