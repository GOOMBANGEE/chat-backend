package com.chat.dto.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInfoForDirectMessageChannelCreateDto {

  private Long userId;
  private String username;
  private String avatarImageSmall;

  @Builder
  public UserInfoForDirectMessageChannelCreateDto(Long userId, String username,
      String avatarImageSmall) {
    this.userId = userId;
    this.username = username;
    this.avatarImageSmall = avatarImageSmall;
  }
}
