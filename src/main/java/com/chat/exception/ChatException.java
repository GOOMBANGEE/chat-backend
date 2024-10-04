package com.chat.exception;

import com.chat.dto.ErrorResponseDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChatException extends RuntimeException {

  private final String id;

  public ErrorResponseDto handleException() {
    return switch (this.id) {
      case "CHAT:CHAT_NOT_FOUND" -> ErrorResponseDto.build(id, "채팅 정보가 없습니다");
      case "CHAT:UNSUPPORTED_FILE_TYPE" -> ErrorResponseDto.build(id, "지원하지 않는 파일형식입니다");
      default -> ErrorResponseDto.build("CHAT:UNKNOWN", "알 수 없는 오류");
    };
  }

}
