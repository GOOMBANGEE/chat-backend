package com.chat.dto.chat;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatListResponseDto {

  private List<ChatInfoDto> chatList;

  @Builder
  public ChatListResponseDto(List<ChatInfoDto> chatList) {
    this.chatList = chatList;
  }
}
