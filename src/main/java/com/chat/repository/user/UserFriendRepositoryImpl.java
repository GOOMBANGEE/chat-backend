package com.chat.repository.user;

import com.chat.domain.user.QUserFriend;
import com.chat.domain.user.User;
import com.chat.domain.user.UserFriend;
import com.chat.dto.user.QUserInfoForFriendListResponseDto;
import com.chat.dto.user.UserInfoForFriendListResponseDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserFriendRepositoryImpl implements UserFriendRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  QUserFriend qUserFriend = QUserFriend.userFriend;

  @Override
  public Optional<UserFriend> fetchByUserAndFriend(User user, User friend) {
    return Optional.ofNullable(queryFactory
        .select(qUserFriend)
        .from(qUserFriend)
        .where(userFriendOrFriendUser(user, friend))
        .fetchFirst());
  }

  private BooleanExpression userFriendOrFriendUser(User user, User friend) {
    return userFriendEq(user, friend).or(friendUserEq(user, friend));
  }

  private BooleanExpression userFriendEq(User user, User friend) {
    return qUserFriend.user.eq(user)
        .and(qUserFriend.friend.eq(friend));
  }

  private BooleanExpression friendUserEq(User user, User friend) {
    return qUserFriend.friend.eq(user)
        .and(qUserFriend.user.eq(friend));
  }


  @Override
  public List<UserInfoForFriendListResponseDto> fetchUserInfoDtoListByUser(
      User user) {
    return queryFactory
        .select(new QUserInfoForFriendListResponseDto(qUserFriend.friend.id,
            qUserFriend.friend.username))
        .from(qUserFriend)
        .where(userEq(user))
        .fetch();
  }

  private BooleanExpression userEq(User user) {
    return qUserFriend.user.eq(user);
  }
}
