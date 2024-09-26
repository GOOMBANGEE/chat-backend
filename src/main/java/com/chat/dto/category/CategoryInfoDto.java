package com.chat.dto.category;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryInfoDto {

  private Long id;

  private String name;

  private Double displayOrder;

  private Long serverId;

  @QueryProjection
  @Builder
  public CategoryInfoDto(Long id, String name, Double displayOrder, Long serverId) {
    this.id = id;
    this.name = name;
    this.displayOrder = displayOrder;
    this.serverId = serverId;
  }
}
