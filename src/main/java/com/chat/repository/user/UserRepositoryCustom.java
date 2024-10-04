package com.chat.repository.user;

import com.chat.dto.user.UserAndServerIdForTimeoutCheckDto;
import java.time.LocalDateTime;
import java.util.List;

public interface UserRepositoryCustom {

  // 등록된 상태, 최근 timeout 시간안에 갱신되지않은 상태, online 상태 -> offline 메시지 발송이 되지않은 상태
  List<UserAndServerIdForTimeoutCheckDto> fetchUserAndUserIdForTimeoutCheckDto(LocalDateTime time);
}
