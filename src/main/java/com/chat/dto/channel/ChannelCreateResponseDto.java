package com.chat.dto.channel;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChannelCreateResponseDto {

  private Long id;

  private String name;

  private Double displayOrder;

  private Long serverId;

  private Long categoryId;

  private Long mentionedUserId;
  private String mentionedUsername;
  private String mentionedUserAvatar;

  private List<Long> allowRoleIdList;

  private List<Long> allowUserIdList;


  @Builder
  public ChannelCreateResponseDto(Long id, String name, Double displayOrder,
      Long serverId, Long categoryId,
      Long mentionedUserId, String mentionedUsername, String mentionedUserAvatar,
      List<Long> allowRoleIdList, List<Long> allowUserIdList) {
    this.id = id;
    this.name = name;
    this.displayOrder = displayOrder;
    this.serverId = serverId;
    this.categoryId = categoryId;
    this.mentionedUserId = mentionedUserId;
    this.mentionedUsername = mentionedUsername;
    this.mentionedUserAvatar = mentionedUserAvatar;
    this.allowRoleIdList = allowRoleIdList;
    this.allowUserIdList = allowUserIdList;
  }
}
