package com.chat.exception;

import com.chat.dto.ErrorResponseDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ServerException extends RuntimeException {

  private final String id;

  public ErrorResponseDto handleException() {
    return switch (this.id) {
      case "SERVER:SERVER_NOT_FOUND" -> ErrorResponseDto.build(id, "서버 정보가 없습니다");
      case "SERVER:SERVER_NAME_INVALID" -> ErrorResponseDto.build(id, "서버 이름이 올바르지 않습니다");
      case "SERVER:SERVER_NOT_PARTICIPATED" -> ErrorResponseDto.build(id, "서버에 참가하지 않은 상태입니다");
      case "SERVER:SERVER_NOT_PERMITTED" -> ErrorResponseDto.build(id, "권한이 없습니다");
      case "SERVER:SERVER_ALREADY_JOINED" -> ErrorResponseDto.build(id, "이미 참여중인 서버입니다");
      case "VALID:PAGE_INVALID" -> ErrorResponseDto.build(id, "페이지가 유효하지 않습니다");
      case "SERVER:SERVER_NOT_EMPTY" -> ErrorResponseDto.build(id, "서버에 유저가 남아있습니다");
      case "SERVER:INVALID_PATH" -> ErrorResponseDto.build(id, "올바르지 않은 경로입니다");
      case "SERVER:INVALID_TOKEN" -> ErrorResponseDto.build(id, "올바르지 않은 토큰입니다");
      case "SERVER:NO_CHANNEL_CREATE_PERMISSION" -> ErrorResponseDto.build(id, "채널 생성권한이 없습니다");
      case "SERVER:UNSUPPORTED_FILE_TYPE" -> ErrorResponseDto.build(id, "지원하지않는 파일형식입니다");
      case "SERVER:IMAGE_SAVE_ERROR" -> ErrorResponseDto.build(id, "이미지 저장중 오류가 발생했습니다");
      default -> ErrorResponseDto.build("SERVER:UNKNOWN", "알 수 없는 오류");
    };
  }
}