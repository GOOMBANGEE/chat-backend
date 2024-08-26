package com.chat.exception;


import com.chat.dto.ErrorResponseDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserException extends RuntimeException {

  private final String id;

  public ErrorResponseDto handleException() {
    return switch (this.id) {
      case "USER:TOKEN_INVALID" -> ErrorResponseDto.build(id, "토큰이 유효하지 않습니다");
      case "USER:EMAIL_EXIST" -> ErrorResponseDto.build(id, "이미 존재하는 이메일입니다");
      case "USER:USERNAME_EXIST" -> ErrorResponseDto.build(id, "이미 존재하는 사용자명입니다");
      case "USER:USER_UNREGISTERED" -> ErrorResponseDto.build(id, "유저 정보가 없습니다");
      case "USER:PASSWORD_MISMATCH" -> ErrorResponseDto.build(id, "비밀번호가 일치하지 않습니다");
      case "USER:EMAIL_OR_PASSWORD_ERROR" -> ErrorResponseDto.build(id, "이메일 혹은 비밀번호가 틀렸습니다");
      case "USER:EMAIL_ACTIVATE_REQUIRE" -> ErrorResponseDto.build(id, "이메일 활성화가 필요합니다");
      case "USER:CODE_INVALID" -> ErrorResponseDto.build(id, "인증코드가 유효하지 않습니다");
      case "USER:GET_AUTHENTICATION_FAIL" -> ErrorResponseDto.build(id, "인증정보가 유효하지 않습니다");

      default -> ErrorResponseDto.build("USER:UNKNOWN", "알 수 없는 오류");
    };
  }
}