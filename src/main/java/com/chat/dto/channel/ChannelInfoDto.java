package com.chat.dto.channel;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ChannelInfoDto {
  
  @EqualsAndHashCode.Include
  private Long id;

  private String name;

  private Double displayOrder;

  private Long serverId;

  private Long categoryId;

  @QueryProjection
  @Builder
  public ChannelInfoDto(Long id, String name, Double displayOrder, Long serverId, Long categoryId) {
    this.id = id;
    this.name = name;
    this.displayOrder = displayOrder;
    this.serverId = serverId;
    this.categoryId = categoryId;
  }
}
