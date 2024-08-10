package com.chat.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponseDto {

  private String id;
  private String message;


  public static ErrorResponseDto build(String id, String message) {
    return ErrorResponseDto.builder().id(id).message(message).build();
  }
}
