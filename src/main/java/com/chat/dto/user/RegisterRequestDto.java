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
public class RegisterRequestDto {

  @NotBlank(message = "VALID:EMAIL_FORM_ERROR")
  @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "VALID:EMAIL_FORM_ERROR")
  private String email;

  @NotBlank(message = "VALID:USERNAME_FORM_ERROR")
  @Size(min = 2, max = 20, message = "VALID:USERNAME_FORM_ERROR")
  private String username;

  @NotBlank(message = "VALID:PASSWORD_FORM_ERROR")
  @Size(min = 8, max = 20, message = "VALID:PASSWORD_FORM_ERROR")
  @Pattern(regexp = ".*[!@#$%^&*()_+\\-=\\[\\]{};':\",.<>/?]+.*", message = "VALID:PASSWORD_FORM_ERROR")
  private String password;

  @NotBlank(message = "VALID:PASSWORD_FORM_ERROR")
  @Size(min = 8, max = 20, message = "VALID:PASSWORD_FORM_ERROR")
  @Pattern(regexp = ".*[!@#$%^&*()_+\\-=\\[\\]{};':\",.<>/?]+.*", message = "VALID:PASSWORD_FORM_ERROR")
  private String confirmPassword;

  @Builder
  public RegisterRequestDto(String email, String username, String password,
      String confirmPassword) {
    this.email = email;
    this.username = username;
    this.password = password;
    this.confirmPassword = confirmPassword;
  }
}