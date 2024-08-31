package com.chat.repository.user;

import com.chat.domain.user.User;
import com.chat.domain.user.UserFriend;
import com.chat.dto.user.UserInfoForFriendListResponseDto;
import java.util.List;
import java.util.Optional;

public interface UserFriendRepositoryCustom {


  Optional<UserFriend> fetchByUserAndFriend(User user, User friend);

  List<UserInfoForFriendListResponseDto> fetchUserInfoDtoListByUser(User user);
}
