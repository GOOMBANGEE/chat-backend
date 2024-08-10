package com.chat.dto;


import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JwtTokenDto {

  private String accessToken;
  private String refreshToken;

  @Builder
  public JwtTokenDto(String accessToken, String refreshToken) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
  }
}
