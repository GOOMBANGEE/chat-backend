package com.chat.dto.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendRequestDto {

  private Long id;
  private String username;
  private String avatar;

  private String friendName;

  @Builder
  public FriendRequestDto(Long id, String username, String avatar, String friendName) {
    this.id = id;
    this.username = username;
    this.avatar = avatar;
    this.friendName = friendName;
  }
}
