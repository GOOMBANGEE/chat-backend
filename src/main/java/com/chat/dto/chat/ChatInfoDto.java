package com.chat.dto.chat;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatInfoDto {

  private Long id;

  private String username;

  private String message;

  private boolean enter;

  private LocalDateTime createTime;

  private LocalDateTime updateTime;

  @QueryProjection
  @Builder
  public ChatInfoDto(Long id, String username, String message, boolean enter,
      LocalDateTime createTime, LocalDateTime updateTime) {
    this.id = id;
    this.username = username;
    this.message = message;
    this.enter = enter;
    this.createTime = createTime;
    this.updateTime = updateTime;
  }
}
