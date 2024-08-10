package com.chat.dto.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileResponseDto {

  private Long id;
  private String email;
  private String username;

  @Builder
  public ProfileResponseDto(Long id, String email, String username) {
    this.id = id;
    this.email = email;
    this.username = username;
  }
}
