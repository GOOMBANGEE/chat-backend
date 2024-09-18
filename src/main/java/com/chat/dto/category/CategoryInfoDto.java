package com.chat.dto.category;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CategoryInfoDto {

  @EqualsAndHashCode.Include
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
