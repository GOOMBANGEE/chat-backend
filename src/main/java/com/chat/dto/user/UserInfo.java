package com.chat.dto.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInfo {

  private Long id;
  private String username;
  private String avatar;

  @Builder
  public UserInfo(Long id, String username, String avatar) {
    this.id = id;
    this.username = username;
    this.avatar = avatar;
  }
}
