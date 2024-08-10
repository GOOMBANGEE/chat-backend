package com.chat.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshRequestDto {

  @NotBlank(message = "VALID:TOKEN_INVALID")
  private String refreshToken;

  @Builder
  public RefreshRequestDto(String refreshToken) {
    this.refreshToken = refreshToken;
  }
}
