package com.chat.dto.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegisterTokenCheckResponseDto {

  private String email;

  @Builder
  public RegisterTokenCheckResponseDto(String email) {
    this.email = email;
  }
}
