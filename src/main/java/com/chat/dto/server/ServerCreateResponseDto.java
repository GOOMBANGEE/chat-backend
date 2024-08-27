package com.chat.dto.server;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerCreateResponseDto {

  private Long id;

  private String name;

  @Builder
  public ServerCreateResponseDto(Long id, String name) {
    this.id = id;
    this.name = name;
  }
}
