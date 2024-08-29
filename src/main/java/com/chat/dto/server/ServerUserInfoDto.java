package com.chat.dto.server;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerUserInfoDto {

  private Long id;

  private String username;

  @QueryProjection
  @Builder
  public ServerUserInfoDto(Long id, String username) {
    this.id = id;
    this.username = username;
  }
}
