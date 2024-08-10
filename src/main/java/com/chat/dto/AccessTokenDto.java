package com.chat.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccessTokenDto {

  private String accessToken;

  @Builder
  public AccessTokenDto(String accessToken) {
    this.accessToken = accessToken;
  }
}
