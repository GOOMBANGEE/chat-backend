package com.chat.dto.chat;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SendMessageResponseDto {

  private Long serverId;

  private Long id;

  @Builder
  public SendMessageResponseDto(Long serverId, Long id) {
    this.serverId = serverId;
    this.id = id;
  }
}
