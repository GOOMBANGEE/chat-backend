package com.chat.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecoverEmailSendRequestDto {

  @NotBlank(message = "VALID:EMAIL_FORM_ERROR")
  @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "VALID:EMAIL_FORM_ERROR")
  private String email;

  @Builder
  public RecoverEmailSendRequestDto(String email) {
    this.email = email;
  }
}
