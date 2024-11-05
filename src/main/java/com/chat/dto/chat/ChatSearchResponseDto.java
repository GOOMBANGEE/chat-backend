package com.chat.dto.chat;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatSearchResponseDto {

  private Integer next;

  private Integer previous;

  private Integer total;

  private Integer page;

  private Integer size;

  private List<ChatInfoDto> chatList;

  @Builder
  public ChatSearchResponseDto(Integer next, Integer previous, Integer total, Integer page,
      Integer size, List<ChatInfoDto> chatList) {
    this.next = next;
    this.previous = previous;
    this.total = total;
    this.page = page;
    this.size = size;
    this.chatList = chatList;
  }
}
