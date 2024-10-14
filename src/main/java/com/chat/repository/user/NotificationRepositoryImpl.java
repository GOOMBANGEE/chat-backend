package com.chat.repository.user;

import com.chat.domain.user.QNotification;
import com.chat.dto.user.NotificationDirectMessageInfoDto;
import com.chat.dto.user.NotificationServerInfoDto;
import com.chat.dto.user.QNotificationDirectMessageInfoDto;
import com.chat.dto.user.QNotificationServerInfoDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  QNotification qNotification = QNotification.notification;

  @Override
  public List<NotificationDirectMessageInfoDto> fetchNotificationInfoDirectMessageDtoByUserEmail(
      String email) {
    return queryFactory
        .select(new QNotificationDirectMessageInfoDto(
            qNotification.channel.id,
            qNotification.channel.name,
            qNotification.chat.id,
            qNotification.chat.message,
            qNotification.chat.attachment,
            qNotification.chat.createTime,
            qNotification.chat.updateTime,
            qNotification.user.id,
            qNotification.user.username,
            qNotification.user.avatarImageSmall
        ))
        .from(qNotification)
        .where(chatLogicDeleteFalse(), mentionedUserEmailEq(email))
        .limit(10)
        .fetch();
  }

  @Override
  public List<NotificationServerInfoDto> fetchNotificationServerInfoDtoByUserEmail(String email) {
    return queryFactory
        .select(new QNotificationServerInfoDto(
            qNotification.server.id,
            qNotification.server.name,
            qNotification.channel.id,
            qNotification.channel.name,
            qNotification.chat.id,
            qNotification.chat.message,
            qNotification.chat.attachment,
            qNotification.chat.createTime,
            qNotification.chat.updateTime,
            qNotification.user.id,
            qNotification.user.username,
            qNotification.user.avatarImageSmall
        ))
        .from(qNotification)
        .where(chatLogicDeleteFalse(), mentionedUserEmailEq(email))
        .limit(10)
        .fetch();
  }

  private BooleanExpression mentionedUserEmailEq(String email) {
    return qNotification.mentionedUser.email.eq(email);
  }

  private BooleanExpression chatLogicDeleteFalse() {
    return qNotification.chat.logicDelete.isFalse();
  }
}
