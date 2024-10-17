package com.chat.dto.server;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerSettingIconRequestDto {

  private String icon;

  @Builder
  public ServerSettingIconRequestDto(String icon) {
    this.icon = icon;
  }
}
