package com.chat.exception;

import com.chat.dto.ErrorResponseDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CategoryException extends RuntimeException {

  private final String id;

  public ErrorResponseDto handleException() {
    return switch (this.id) {
      case "CATEGORY:CATEGORY_NOT_FOUND" -> ErrorResponseDto.build(id, "카테고리 정보가 없습니다");

      default -> ErrorResponseDto.build("CATEGORY:UNKNOWN", "알 수 없는 오류");
    };
  }

}
