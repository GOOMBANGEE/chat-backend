package com.chat.dto.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendRejectRequestDto {

  private Long id;

  @Builder
  public FriendRejectRequestDto(Long id) {
    this.id = id;
  }
}
