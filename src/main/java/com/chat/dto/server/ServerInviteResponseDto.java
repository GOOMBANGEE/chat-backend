package com.chat.dto.server;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerInviteResponseDto {

  private String link;

  @Builder
  public ServerInviteResponseDto(String link) {
    this.link = link;
  }
}
