package com.chat.dto.channel;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChannelCreateRequestDto {

  private String name;

  private List<Long> allowRoleIdList;

  private List<Long> allowUserIdList;

  private Long serverId;

  private Long categoryId;

  private Long userId;

  @Builder
  public ChannelCreateRequestDto(String name, List<Long> allowRoleIdList,
      List<Long> allowUserIdList, Long serverId, Long categoryId, Long userId) {
    this.name = name;
    this.allowRoleIdList = allowRoleIdList;
    this.allowUserIdList = allowUserIdList;
    this.serverId = serverId;
    this.categoryId = categoryId;
    this.userId = userId;
  }
}
