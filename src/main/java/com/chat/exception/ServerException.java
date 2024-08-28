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
      case "SERVER:USER_UNREGISTERED" -> ErrorResponseDto.build(id, "유저 정보가 없습니다");
      case "SERVER:SERVER_NOT_FOUND" -> ErrorResponseDto.build(id, "서버 정보가 없습니다");
      case "SERVER:SERVER_NAME_INVALID" -> ErrorResponseDto.build(id, "서버 이름이 올바르지 않습니다");
      case "SERVER:SERVER_NOT_PARTICIPATED" -> ErrorResponseDto.build(id, "서버에 참가하지 않은 상태입니다");
      case "SERVER:SERVER_NOT_PERMITTED" -> ErrorResponseDto.build(id, "권한이 없습니다");
      case "SERVER:SERVER_ALREADY_JOINED" -> ErrorResponseDto.build(id, "이미 참여중인 서버입니다");
      case "SERVER:CHAT_NOT_FOUND" -> ErrorResponseDto.build(id, "채팅 정보가 없습니다");
      default -> ErrorResponseDto.build("USER:UNKNOWN", "알 수 없는 오류");
    };
  }
}