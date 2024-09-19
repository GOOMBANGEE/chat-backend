package com.chat.dto.channel;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChannelSettingResponseDto {

  private Long id;

  private String name;

  private Double displayOrder;

  private boolean open;

  private Long categoryId;

  @Builder
  public ChannelSettingResponseDto(Long id, String name, Double displayOrder, boolean open,
      Long categoryId) {
    this.id = id;
    this.name = name;
    this.displayOrder = displayOrder;
    this.open = open;
    this.categoryId = categoryId;
  }
}
