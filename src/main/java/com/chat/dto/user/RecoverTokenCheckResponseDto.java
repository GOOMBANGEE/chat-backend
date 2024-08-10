package com.chat.dto.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecoverTokenCheckResponseDto {

  private String email;

  @Builder
  public RecoverTokenCheckResponseDto(String email) {
    this.email = email;
  }
}
