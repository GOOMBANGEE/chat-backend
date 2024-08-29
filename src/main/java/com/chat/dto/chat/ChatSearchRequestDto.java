package com.chat.dto.chat;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatSearchRequestDto {

  private String keyword;

  private String username;

  private String message;

  @Builder
  public ChatSearchRequestDto(String keyword, String username, String message) {
    this.keyword = keyword;
    this.username = username;
    this.message = message;
  }
}
