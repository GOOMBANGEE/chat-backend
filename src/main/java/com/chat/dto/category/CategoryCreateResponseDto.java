package com.chat.dto.category;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryCreateResponseDto {

  private Long id;

  private String name;

  private Double displayOrder;

  private Long serverId;

  private List<Long> allowRoleIdList;

  private List<Long> allowUserIdList;

  @Builder
  public CategoryCreateResponseDto(Long id, String name, Double displayOrder, Long serverId,
      List<Long> allowRoleIdList, List<Long> allowUserIdList) {
    this.id = id;
    this.name = name;
    this.displayOrder = displayOrder;
    this.serverId = serverId;
    this.allowRoleIdList = allowRoleIdList;
    this.allowUserIdList = allowUserIdList;
  }
}
