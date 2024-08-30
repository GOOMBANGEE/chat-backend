package com.chat.repository.user;

import com.chat.domain.user.User;
import com.chat.domain.user.UserFriendTemp;
import com.chat.dto.user.UserInfoForFriendWaitingListResponseDto;
import java.util.List;
import java.util.Optional;

public interface UserFriendTempRepositoryCustom {

  // 친구신청이 들어왔는지 확인하는 쿼리
  Optional<UserFriendTemp> fetchByUserAndFriend(User user, User friend);

  List<UserInfoForFriendWaitingListResponseDto> fetchUserInfoByUser(User user);
  
}
