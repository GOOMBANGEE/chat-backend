package com.chat.dto.server;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerDeleteRequestDto {

  private String name;

  @Builder
  public ServerDeleteRequestDto(String name) {
    this.name = name;
  }
}
