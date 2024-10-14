package com.chat.dto.user;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInfoForFriendWaitingListResponseDto {

  private Long id;

  private String username;

  private String avatarImageSmall;

  private Boolean online;

  @QueryProjection
  @Builder
  public UserInfoForFriendWaitingListResponseDto(Long id, String username, String avatarImageSmall,
      Boolean online) {
    this.id = id;
    this.username = username;
    this.avatarImageSmall = avatarImageSmall;
    this.online = online;
  }
}
