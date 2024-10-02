package com.chat.dto.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChangeAvatarRequestDto {

  private Long id;

  private String avatar;

  @Builder
  public ChangeAvatarRequestDto(Long id, String avatar) {
    this.id = id;
    this.avatar = avatar;
  }
}
