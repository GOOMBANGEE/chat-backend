package com.chat.exception;

import com.chat.dto.ErrorResponseDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MailException extends RuntimeException {

  private final String id;

  public ErrorResponseDto handleMailException() {
    if (this.id.equals("MAIL:SEND_MAIL_FAIL")) {
      return ErrorResponseDto.build(id, "메일 발송중 오류가 발생했습니다");
    }
    return ErrorResponseDto.build("UNKNOWN", "알 수 없는 오류");
  }
}
