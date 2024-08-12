package com.chat.exception;

import com.chat.dto.ErrorResponseDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ServerException extends RuntimeException {

  private final String id;

  public ErrorResponseDto handleUserException() {
    return switch (this.id) {
      case "SERVER:USER_UNREGISTERED" -> ErrorResponseDto.build(id, "유저 정보가 없습니다");
      default -> ErrorResponseDto.build("USER:UNKNOWN", "알 수 없는 오류");
    };
  }
}