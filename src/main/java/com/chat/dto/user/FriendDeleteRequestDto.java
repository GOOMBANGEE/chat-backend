package com.chat.dto.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendDeleteRequestDto {

  private Long id;

  private String username;

  private Long friendId;

  @Builder
  public FriendDeleteRequestDto(Long id, String username, Long friendId) {
    this.id = id;
    this.username = username;
    this.friendId = friendId;
  }
}
