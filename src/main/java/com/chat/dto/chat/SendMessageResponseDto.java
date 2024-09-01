package com.chat.dto.chat;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SendMessageResponseDto {

  private Long serverId;

  private Long id;

  private LocalDateTime createTime;

  @Builder
  public SendMessageResponseDto(Long serverId, Long id, LocalDateTime createTime) {
    this.serverId = serverId;
    this.id = id;
    this.createTime = createTime;
  }
}
