package com.chat.dto.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInfoForServerJoinResponseDto {

  private Long userId;

  private String username;

  @Builder
  public UserInfoForServerJoinResponseDto(Long userId, String username) {
    this.userId = userId;
    this.username = username;
  }
}
