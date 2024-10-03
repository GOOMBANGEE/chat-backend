package com.chat.dto.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChangeAvatarResponseDto {

  private String avatarImageSmall;

  @Builder
  public ChangeAvatarResponseDto(String avatarImageSmall) {
    this.avatarImageSmall = avatarImageSmall;
  }
}
