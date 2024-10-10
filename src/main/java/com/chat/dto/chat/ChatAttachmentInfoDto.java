package com.chat.dto.chat;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatAttachmentInfoDto {

  private String mimeType;
  private String filePath;

  private Integer attachmentWidth;
  private Integer attachmentHeight;

  @Builder
  public ChatAttachmentInfoDto(String mimeType, String filePath,
      Integer attachmentWidth, Integer attachmentHeight) {
    this.mimeType = mimeType;
    this.filePath = filePath;
    this.attachmentWidth = attachmentWidth;
    this.attachmentHeight = attachmentHeight;
  }
}
