package com.chat.dto.user;

import com.chat.domain.user.User;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAndServerIdForTimeoutCheckDto {

  private User user;

  private Long userId;

  private Long serverId;

  @QueryProjection
  @Builder
  public UserAndServerIdForTimeoutCheckDto(User user, Long userId, Long serverId) {
    this.user = user;
    this.userId = userId;
    this.serverId = serverId;
  }
}
