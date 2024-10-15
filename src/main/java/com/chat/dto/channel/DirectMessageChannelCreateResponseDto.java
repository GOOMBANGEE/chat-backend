package com.chat.dto.channel;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DirectMessageChannelCreateResponseDto {

  private Long id;
  // 요청 보낸사람정보
  private Long userId;
  private String username;
  private String avatar;

  @Builder
  public DirectMessageChannelCreateResponseDto(Long id, Long userId, String username,
      String avatar) {
    this.id = id;
    this.userId = userId;
    this.username = username;
    this.avatar = avatar;
  }
}
