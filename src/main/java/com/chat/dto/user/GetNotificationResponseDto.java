package com.chat.dto.user;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GetNotificationResponseDto {

  private List<NotificationDirectMessageInfoDto> notificationDirectMessageInfoDtoList;

  private List<NotificationServerInfoDto> notificationServerInfoDtoList;

  @Builder
  public GetNotificationResponseDto(
      List<NotificationDirectMessageInfoDto> notificationDirectMessageInfoDtoList,
      List<NotificationServerInfoDto> notificationServerInfoDtoList) {
    this.notificationDirectMessageInfoDtoList = notificationDirectMessageInfoDtoList;
    this.notificationServerInfoDtoList = notificationServerInfoDtoList;
  }
}
