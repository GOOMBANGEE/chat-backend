package com.chat.dto.server;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerCreateResponseDto {

  private Long id;
  private String name;

  private Long categoryId;
  private String categoryName;
  private Double categoryDisplayOrder;

  private Long channelId;
  private String channelName;
  private Double channelDisplayOrder;

  @Builder
  public ServerCreateResponseDto(Long id, String name, Long categoryId, String categoryName,
      Double categoryDisplayOrder, Long channelId, String channelName, Double channelDisplayOrder) {
    this.id = id;
    this.name = name;
    this.categoryId = categoryId;
    this.categoryName = categoryName;
    this.categoryDisplayOrder = categoryDisplayOrder;
    this.channelId = channelId;
    this.channelName = channelName;
    this.channelDisplayOrder = channelDisplayOrder;
  }
}
