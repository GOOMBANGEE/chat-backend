package com.chat.dto.server;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerCreateRequestDto {

  private Long userId;

  private String username;

  private String name;

  @Builder
  public ServerCreateRequestDto(Long userId, String username, String name) {
    this.userId = userId;
    this.username = username;
    this.name = name;
  }
}
