package com.chat.exception;


import com.chat.dto.ErrorResponseDto;

public class ValidExceptionHandler {

  public static ErrorResponseDto handleMethodArgumentNotValidException(String id) {
    return switch (id) {
      case "VALID:EMAIL_FORM_ERROR" -> ErrorResponseDto.build(id, "이메일 유효성 검사를 통과하지 못했습니다");
      case "VALID:USERNAME_FORM_ERROR" -> ErrorResponseDto.build(id, "사용자명 유효성 검사를 통과하지 못했습니다");
      case "VALID:PASSWORD_FORM_ERROR" -> ErrorResponseDto.build(id, "비밀번호 유효성 검사를 통과하지 못했습니다");
      case "VALID:EMAIL_INVALID" -> ErrorResponseDto.build(id, "이메일이 유효하지 않습니다");
      case "VALID:TOKEN_INVALID" -> ErrorResponseDto.build(id, "토큰이 유효하지 않습니다");
      case "VALID:CODE_INVALID" -> ErrorResponseDto.build(id, "코드가 유효하지 않습니다");
      case "VALID:VALUE_INVALID" -> ErrorResponseDto.build(id, "값이 유효하지 않습니다");
      case "VALID:SERVER_INVALID" -> ErrorResponseDto.build(id, "서버가 유효하지 않습니다");
      case "VALID:CATEGORY_INVALID" -> ErrorResponseDto.build(id, "카테고리가 유효하지 않습니다");
      case "VALID:CHAT_INVALID" -> ErrorResponseDto.build(id, "채팅이 유효하지 않습니다");
      case "VALID:PAGE_INVALID" -> ErrorResponseDto.build(id, "페이지가 유효하지 않습니다");
      case "VALID:CHANNEL_INVALID" -> ErrorResponseDto.build(id, "채널이 유효하지 않습니다");
      default -> ErrorResponseDto.build("VALID:UNKNOWN", "알 수 없는 오류");
    };
  }
}
