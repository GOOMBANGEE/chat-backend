package com.chat.dto.chat;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SendMessageResponseDto {

  private Long serverId;

  private Long channelId;

  private Long id;

  private LocalDateTime createTime;

  private String avatar;

  private String attachment;

  @Builder
  public SendMessageResponseDto(Long serverId, Long channelId, Long id, LocalDateTime createTime,
      String avatar, String attachment) {
    this.serverId = serverId;
    this.channelId = channelId;
    this.id = id;
    this.createTime = createTime;
    this.avatar = avatar;
    this.attachment = attachment;
  }
}
