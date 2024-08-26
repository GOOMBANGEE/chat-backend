package com.chat.dto.server;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerInviteInfoResponseDto {

  private String name;

  private String username;

  private Long userCount;

  @Builder
  public ServerInviteInfoResponseDto(String name, String username, Long userCount) {
    this.name = name;
    this.username = username;
    this.userCount = userCount;
  }
}
