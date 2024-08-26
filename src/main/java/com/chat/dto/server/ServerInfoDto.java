package com.chat.dto.server;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerInfoDto {

  private Long id;

  private String name;

  @QueryProjection
  @Builder
  public ServerInfoDto(Long id, String name) {
    this.id = id;
    this.name = name;
  }
}
