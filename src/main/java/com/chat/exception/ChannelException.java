package com.chat.exception;

import com.chat.dto.ErrorResponseDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChannelException extends RuntimeException {

  private final String id;

  public ErrorResponseDto handleException() {
    return switch (this.id) {
      case "CHANNEL:CHANNEL_NOT_FOUND" -> ErrorResponseDto.build(id, "채널 정보가 없습니다");
      default -> ErrorResponseDto.build("CHANNEL:UNKNOWN", "알 수 없는 오류");
    };
  }

}
