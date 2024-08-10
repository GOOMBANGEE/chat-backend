package com.chat.exception;


import com.chat.dto.ErrorResponseDto;

public class ValidatedExceptionHandler {

  public static ErrorResponseDto handleConstraintViolationException(String id) {
    return switch (id) {

      default -> ErrorResponseDto.build("VALID:UNKNOWN", "알 수 없는 오류");
    };
  }
}
