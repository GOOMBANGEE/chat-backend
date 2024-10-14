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
      case "USER:USER_NOT_FOUND" -> ErrorResponseDto.build(id, "유저 정보를 찾을 수 없습니다");
      case "USER:IMAGE_INVALID" -> ErrorResponseDto.build(id, "이미지 형식이 올바르지 않습니다");
      case "USER:IMAGE_SAVE_ERROR" -> ErrorResponseDto.build(id, "이미지 저장중 오류가 발생했습니다");
      case "USER:IMAGE_DELETE_ERROR" -> ErrorResponseDto.build(id, "기존 이미지 삭제중 오류가 발생했습니다");
      case "USER:NOT_ALLOWED_APPLY_YOURSELF" ->
          ErrorResponseDto.build(id, "자기 자신에게 신청하는것은 허용되지않습니다");
      case "USER:USER_ALREADY_FRIEND" -> ErrorResponseDto.build(id, "이미 등록된 유저입니다");
      case "USER:USER_ALREADY_SENT_REQUEST" -> ErrorResponseDto.build(id, "상대방의 응답을 기다리는중입니다");
      case "USER:USER_FRIEND_TEMP_NOT_FOUND" -> ErrorResponseDto.build(id, "존재하지 않는 요청입니다");
      default -> ErrorResponseDto.build("USER:UNKNOWN", "알 수 없는 오류");
    };
  }
}