package com.chat.repository.user;

import com.chat.domain.user.QUser;
import com.chat.domain.user.QUserFriendTemp;
import com.chat.domain.user.User;
import com.chat.domain.user.UserFriendTemp;
import com.chat.dto.user.QUserInfoForFriendWaitingListResponseDto;
import com.chat.dto.user.UserInfoForFriendWaitingListResponseDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserFriendTempQueryRepository {

  private final JPAQueryFactory queryFactory;
  QUserFriendTemp qUserFriendTemp = QUserFriendTemp.userFriendTemp;
  QUser qUser = QUser.user;

  public Optional<UserFriendTemp> fetchByUserAndFriend(User user, User friend) {
    return Optional.ofNullable(queryFactory
        .select(qUserFriendTemp)
        .from(qUserFriendTemp)
        .where(userFriendOrFriendUser(user, friend))
        .fetchFirst());
  }

  private BooleanExpression userFriendOrFriendUser(User user, User friend) {
    return userFriendEq(user, friend).or(friendUserEq(user, friend));
  }

  private BooleanExpression userFriendEq(User user, User friend) {
    return qUserFriendTemp.user.eq(user)
        .and(qUserFriendTemp.friend.eq(friend));
  }

  private BooleanExpression friendUserEq(User user, User friend) {
    return qUserFriendTemp.friend.eq(user)
        .and(qUserFriendTemp.user.eq(friend));
  }

  // 친구 신청을 받았을때는 내가 friend에, 상대가 user에 위치해있다
  public List<UserInfoForFriendWaitingListResponseDto> fetchUserInfoByUser(User user) {
    return queryFactory
        .select(new QUserInfoForFriendWaitingListResponseDto(
            qUserFriendTemp.user.id,
            qUserFriendTemp.user.username,
            qUserFriendTemp.user.avatarImageSmall,
            qUserFriendTemp.user.online))
        .from(qUserFriendTemp)
        .where(friendEq(user))
        .join(qUserFriendTemp.user, qUser)
        .fetch();
  }

  private BooleanExpression friendEq(User user) {
    return qUserFriendTemp.friend.eq(user);
  }
}
