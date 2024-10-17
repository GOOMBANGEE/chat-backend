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

  private String icon;

  @Builder
  public ServerCreateRequestDto(Long userId, String username, String name, String icon) {
    this.userId = userId;
    this.username = username;
    this.name = name;
    this.icon = icon;
  }
}
