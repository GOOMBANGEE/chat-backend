package com.chat.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecoverConfirmRequestDto {

  @NotBlank(message = "VALID:TOKEN_INVALID")
  private String token;

  @NotBlank(message = "VALID:EMAIL_FORM_ERROR")
  @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "VALID:EMAIL_FORM_ERROR")
  private String email;

  @NotBlank(message = "VALID:PASSWORD_FORM_ERROR")
  @Size(min = 8, max = 20, message = "VALID:PASSWORD_FORM_ERROR")
  @Pattern(regexp = ".*[!@#$%^&*()_+\\-=\\[\\]{};':\",.<>/?]+.*", message = "VALID:PASSWORD_FORM_ERROR")
  private String password;

  @Builder
  public RecoverConfirmRequestDto(String token, String email, String password) {
    this.token = token;
    this.email = email;
    this.password = password;
  }
}
