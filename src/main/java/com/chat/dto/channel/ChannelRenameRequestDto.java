package com.chat.dto.channel;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChannelRenameRequestDto {

  private String name;

  @Builder
  public ChannelRenameRequestDto(String name) {
    this.name = name;
  }
}
