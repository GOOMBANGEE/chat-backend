package com.chat.dto.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInfoForServerJoinResponseDto {

  private Long id;

  private String username;

  @Builder
  public UserInfoForServerJoinResponseDto(Long id, String username) {
    this.id = id;
    this.username = username;
  }
}
