package com.chat.dto.server;

import com.chat.domain.user.User;
import com.querydsl.core.annotations.QueryProjection;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerIdListByUserDto {

  private User user;

  private Long userId;

  private List<Long> serverIdList;

  @QueryProjection
  @Builder
  public ServerIdListByUserDto(User user, Long userId, List<Long> serverIdList) {
    this.user = user;
    this.userId = userId;
    this.serverIdList = serverIdList;
  }
}
