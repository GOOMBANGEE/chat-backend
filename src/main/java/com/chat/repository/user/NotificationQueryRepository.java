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
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationQueryRepository {

  private final JPAQueryFactory queryFactory;
  QNotification qNotification = QNotification.notification;

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
        .where(chatLogicDeleteFalse(), mentionedUserEmailEq(email), readFalse())
        .orderBy(qNotification.id.desc())
        .fetch();
  }

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
        .where(chatLogicDeleteFalse(), mentionedUserEmailEq(email), readFalse())
        .orderBy(qNotification.id.desc())
        .limit(10)
        .fetch();
  }

  private BooleanExpression mentionedUserEmailEq(String email) {
    return qNotification.mentionedUser.email.eq(email);
  }

  private BooleanExpression chatLogicDeleteFalse() {
    return qNotification.chat.logicDelete.isFalse();
  }

  private BooleanExpression readFalse() {
    return qNotification.isRead.isFalse();
  }

  public void bulkUpdateRead(Long channelId, String email) {
    queryFactory
        .update(qNotification)
        .set(qNotification.isRead, true)
        .where(channelIdEq(channelId), mentionedUserEmailEq(email))
        .execute();
  }

  private BooleanExpression channelIdEq(Long channelId) {
    return qNotification.channel.id.eq(channelId);
  }
}
