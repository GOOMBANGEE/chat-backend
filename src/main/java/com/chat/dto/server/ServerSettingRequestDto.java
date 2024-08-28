package com.chat.dto.server;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerSettingRequestDto {

  private String name;

  @Builder
  public ServerSettingRequestDto(String name) {
    this.name = name;
  }
}
