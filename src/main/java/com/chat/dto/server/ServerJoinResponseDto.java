package com.chat.dto.server;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerJoinResponseDto {

  private Long id;

  private String name;

  private String icon;

  private Long channelId;

  @Builder
  public ServerJoinResponseDto(Long id, String name, String icon, Long channelId) {
    this.id = id;
    this.name = name;
    this.icon = icon;
    this.channelId = channelId;
  }
}
