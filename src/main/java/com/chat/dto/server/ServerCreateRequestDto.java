package com.chat.dto.server;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerCreateRequestDto {

  private String username;

  private String name;

  @Builder
  public ServerCreateRequestDto(String username, String name) {
    this.username = username;
    this.name = name;
  }
}