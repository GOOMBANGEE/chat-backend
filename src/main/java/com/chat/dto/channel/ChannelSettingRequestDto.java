package com.chat.dto.channel;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChannelSettingRequestDto {

  private String name;

  private Double displayOrder;

  private boolean open;

  private List<Long> allowRoleIdList;

  private List<Long> allowUserIdList;

  private Long categoryId;

  @Builder
  public ChannelSettingRequestDto(String name, Double displayOrder, boolean open,
      List<Long> allowRoleIdList, List<Long> allowUserIdList, Long categoryId) {
    this.name = name;
    this.displayOrder = displayOrder;
    this.open = open;
    this.allowRoleIdList = allowRoleIdList;
    this.allowUserIdList = allowUserIdList;
    this.categoryId = categoryId;
  }
}
