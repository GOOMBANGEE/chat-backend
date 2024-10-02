package com.chat.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChangeUsernameRequestDto {

  private Long id;

  @NotBlank(message = "VALID:USERNAME_FORM_ERROR")
  @Size(min = 2, max = 20, message = "VALID:USERNAME_FORM_ERROR")
  private String username;

  @Builder
  public ChangeUsernameRequestDto(Long id, String username) {
    this.id = id;
    this.username = username;
  }
}
