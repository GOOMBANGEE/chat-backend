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
public class ChangePasswordRequestDto {

  @NotBlank(message = "VALID:PASSWORD_FORM_ERROR")
  @Size(min = 8, max = 20, message = "VALID:PASSWORD_FORM_ERROR")
  @Pattern(regexp = ".*[!@#$%^&*()_+\\-=\\[\\]{};':\",.<>/?]+.*", message = "VALID:PASSWORD_FORM_ERROR")
  private String prevPassword;

  @NotBlank(message = "VALID:PASSWORD_FORM_ERROR")
  @Size(min = 8, max = 20, message = "VALID:PASSWORD_FORM_ERROR")
  @Pattern(regexp = ".*[!@#$%^&*()_+\\-=\\[\\]{};':\",.<>/?]+.*", message = "VALID:PASSWORD_FORM_ERROR")
  private String newPassword;

  @Builder
  public ChangePasswordRequestDto(String prevPassword, String newPassword) {
    this.prevPassword = prevPassword;
    this.newPassword = newPassword;
  }
}
