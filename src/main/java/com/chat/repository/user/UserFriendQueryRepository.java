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
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserFriendQueryRepository {

  private final JPAQueryFactory queryFactory;
  QUserFriend qUserFriend = QUserFriend.userFriend;

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

  public List<UserFriend> fetchListByUserAndFriend(User user, User friend) {
    return queryFactory
        .select(qUserFriend)
        .from(qUserFriend)
        .where(userFriendOrFriendUser(user, friend))
        .fetch();
  }

  public List<UserInfoForFriendListResponseDto> fetchUserInfoDtoListByUser(
      User user) {
    return queryFactory
        .select(new QUserInfoForFriendListResponseDto(
            qUserFriend.friend.id,
            qUserFriend.friend.username,
            qUserFriend.friend.avatarImageSmall,
            qUserFriend.friend.online))
        .from(qUserFriend)
        .where(userEq(user))
        .fetch();
  }

  private BooleanExpression userEq(User user) {
    return qUserFriend.user.eq(user);
  }

  public void bulkDeleteByUserFriendIn(List<UserFriend> userFriendList) {
    queryFactory
        .delete(qUserFriend)
        .where(userFriendIn(userFriendList))
        .execute();
  }

  private BooleanExpression userFriendIn(List<UserFriend> userFriendList) {
    return qUserFriend.in(userFriendList);
  }
}
