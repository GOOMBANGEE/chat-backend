package com.chat.dto.user;

import com.querydsl.core.annotations.QueryProjection;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendListResponseDto {

  private List<UserInfoForFriendListResponseDto> friendList;

  @QueryProjection
  @Builder
  public FriendListResponseDto(List<UserInfoForFriendListResponseDto> friendList) {
    this.friendList = friendList;
  }
}
