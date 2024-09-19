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

  private Long categoryId;

  @Builder
  public ChannelCreateRequestDto(String name, List<Long> allowRoleIdList,
      List<Long> allowUserIdList, Long categoryId) {
    this.name = name;
    this.allowRoleIdList = allowRoleIdList;
    this.allowUserIdList = allowUserIdList;
    this.categoryId = categoryId;
  }
}
