package com.chat.dto.chat;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatInfoDto {

  private Long id;

  private String username;

  private String message;

  @QueryProjection
  @Builder
  public ChatInfoDto(Long id, String username, String message) {
    this.id = id;
    this.username = username;
    this.message = message;
  }
}
