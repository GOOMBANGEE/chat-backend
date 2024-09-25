package com.chat.dto.category;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryCreateRequest {

  private String name;

  private List<Long> allowRoleIdList;

  private List<Long> allowUserIdList;

  @Builder
  public CategoryCreateRequest(String name, List<Long> allowRoleIdList,
      List<Long> allowUserIdList) {
    this.name = name;
    this.allowRoleIdList = allowRoleIdList;
    this.allowUserIdList = allowUserIdList;
  }
}
