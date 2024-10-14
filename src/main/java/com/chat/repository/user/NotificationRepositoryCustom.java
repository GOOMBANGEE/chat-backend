package com.chat.repository.user;

import com.chat.dto.user.NotificationDirectMessageInfoDto;
import com.chat.dto.user.NotificationServerInfoDto;
import java.util.List;

public interface NotificationRepositoryCustom {

  List<NotificationDirectMessageInfoDto> fetchNotificationInfoDirectMessageDtoByUserEmail(
      String email);

  List<NotificationServerInfoDto> fetchNotificationServerInfoDtoByUserEmail(String email);
}
