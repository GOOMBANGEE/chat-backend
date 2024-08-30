package com.chat.dto.user;

import com.querydsl.core.annotations.QueryProjection;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendWaitingListResponseDto {

  private List<UserInfoForFriendWaitingListResponseDto> waitingList;

  @QueryProjection
  @Builder
  public FriendWaitingListResponseDto(List<UserInfoForFriendWaitingListResponseDto> waitingList) {
    this.waitingList = waitingList;
  }
}
