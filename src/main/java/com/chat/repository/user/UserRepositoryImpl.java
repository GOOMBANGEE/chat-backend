package com.chat.repository.user;

import com.chat.domain.server.QServer;
import com.chat.domain.server.QServerUserRelation;
import com.chat.domain.user.QUser;
import com.chat.dto.user.QUserAndServerIdForTimeoutCheckDto;
import com.chat.dto.user.UserAndServerIdForTimeoutCheckDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  QServerUserRelation qServerUserRelation = QServerUserRelation.serverUserRelation;
  QServer qServer = QServer.server;
  QUser qUser = QUser.user;

  // 등록된 상태, 최근 timeout 시간안에 갱신되지않은 상태, online 상태 -> offline 메시지 발송이 되지않은 상태
  @Override
  public List<UserAndServerIdForTimeoutCheckDto> fetchUserAndUserIdForTimeoutCheckDto(
      LocalDateTime time) {
    return queryFactory
        .select(new QUserAndServerIdForTimeoutCheckDto(qUser, qUser.id, qServer.id))
        .from(qUser)
        .leftJoin(qServerUserRelation).on(qUser.id.eq(qServerUserRelation.user.id)).fetchJoin()
        .where(logicDeleteFalse(), timeoutTrue(time), onlineTrue())
        .fetch();
  }

  private BooleanExpression logicDeleteFalse() {
    return qUser.logicDelete.isFalse();
  }

  private BooleanExpression timeoutTrue(LocalDateTime time) {
    return qUser.lastLogin.before(time);
  }

  private BooleanExpression onlineTrue() {
    return qUser.online.isTrue();
  }
}
