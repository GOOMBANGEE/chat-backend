package com.chat.repository.channel;

import static org.springframework.util.ObjectUtils.isEmpty;

import com.chat.domain.channel.Channel;
import com.chat.domain.channel.ChannelUserRelation;
import com.chat.domain.channel.QChannelUserRelation;
import com.chat.domain.user.QUser;
import com.chat.domain.user.User;
import com.chat.dto.channel.ChannelInfoDto;
import com.chat.dto.channel.ChannelRegistrationDto;
import com.chat.dto.channel.ChannelUserRelationInfoDto;
import com.chat.dto.channel.QChannelInfoDto;
import com.chat.dto.channel.QChannelUserRelationInfoDto;
import com.chat.dto.user.QUserAndServerAndChannelUserRelationForTimeoutCheckDto;
import com.chat.dto.user.UserAndServerAndChannelUserRelationForTimeoutCheckDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChannelUserRelationQueryRepository {

  private final JPAQueryFactory queryFactory;
  private final JdbcTemplate jdbcTemplate;
  QChannelUserRelation qChannelUserRelation = QChannelUserRelation.channelUserRelation;
  QUser qUser = QUser.user;

  public ChannelUserRelationInfoDto fetchChannelUserRelationInfoDtoByServerIdAndChannelIdAndUserEmail(
      Long serverId, Long channelId, String email) {
    if (serverId == null) {
      return queryFactory
          .select(new QChannelUserRelationInfoDto(
              qChannelUserRelation.channel,
              qChannelUserRelation,
              qChannelUserRelation.user
          ))
          .from(qChannelUserRelation)
          .where(
              channelIdEq(channelId),
              userEmailEq(email)
          )
          .fetchFirst();
    } else {
      return queryFactory
          .select(new QChannelUserRelationInfoDto(
              qChannelUserRelation.channel.server,
              qChannelUserRelation.channel,
              qChannelUserRelation,
              qChannelUserRelation.user
          ))
          .from(qChannelUserRelation)
          .where(
              serverIdEq(serverId),
              channelIdEq(channelId),
              userEmailEq(email)
          )
          .fetchFirst();
    }
  }

  private BooleanExpression serverIdEq(Long serverId) {
    return isEmpty(serverId) ? null : qChannelUserRelation.channel.server.id.eq(serverId);
  }

  private BooleanExpression channelIdEq(Long channelId) {
    return qChannelUserRelation.channel.id.eq(channelId);
  }

  private BooleanExpression userEmailEq(String email) {
    return qChannelUserRelation.user.email.eq(email);
  }

  public Optional<ChannelUserRelation> fetchChannelUserRelationByChannelIdAndUserId(Long channelId,
      Long userId) {
    return Optional.ofNullable(queryFactory
        .selectFrom(qChannelUserRelation)
        .where(channelIdEq(channelId), userIdEq(userId))
        .fetchFirst());
  }

  private BooleanExpression userIdEq(Long userId) {
    return qChannelUserRelation.user.id.eq(userId);
  }

  public List<ChannelInfoDto> fetchChannelInfoDtoListByUser(User user) {
    return queryFactory
        .select(new QChannelInfoDto(
            qChannelUserRelation.channel.id,
            qChannelUserRelation.channel.name,
            qChannelUserRelation.channel.displayOrder,
            qChannelUserRelation.channel.server.id,
            qChannelUserRelation.channel.category.id,
            qChannelUserRelation.lastReadMessageId,
            qChannelUserRelation.channel.lastMessageId))
        .from(qChannelUserRelation)
        .where(userEq(user), logicDeleteFalse())
        .fetch();
  }

  private BooleanExpression userEq(User user) {
    return isEmpty(user) ? null : qChannelUserRelation.user.eq(user);
  }

  private BooleanExpression logicDeleteFalse() {
    return qChannelUserRelation.channel.logicDelete.isFalse();
  }

  public List<ChannelInfoDto> fetchDirectMessageChannelInfoDtoListByUser(User user) {
    return queryFactory
        .select(new QChannelInfoDto(
            qChannelUserRelation.channel.id,
            qChannelUserRelation.channel.name,
            qChannelUserRelation.channel.displayOrder,
            qChannelUserRelation.channel.server.id,
            qChannelUserRelation.channel.category.id,
            qChannelUserRelation.lastReadMessageId,
            qChannelUserRelation.channel.lastMessageId,
            qChannelUserRelation.userDirectMessage.id,
            qChannelUserRelation.userDirectMessage.username,
            qChannelUserRelation.userDirectMessage.avatarImageSmall))
        .from(qChannelUserRelation)
        .where(userEq(user), logicDeleteFalse())
        .fetch();
  }

  public List<User> fetchUserListByChannel(Channel channel) {
    return queryFactory
        .select(qChannelUserRelation.user)
        .from(qChannelUserRelation)
        .where(channelEq(channel), logicDeleteFalse())
        .fetch();
  }

  private BooleanExpression channelEq(Channel channel) {
    return isEmpty(channel) ? null : qChannelUserRelation.channel.eq(channel);
  }

  // 두 유저가 속해있는 dm채널이 있는지 확인
  public Optional<ChannelUserRelation> searchDirectMessageChannel(User user, User mentionedUser) {
    return Optional.ofNullable(queryFactory
        .select(qChannelUserRelation)
        .from(qChannelUserRelation)
        .where(userEq(user), mentionedUserEq(mentionedUser))
        .fetchFirst());
  }

  private BooleanExpression mentionedUserEq(User mentionedUser) {
    return qChannelUserRelation.userDirectMessage.eq(mentionedUser);
  }

  // 접속해있지만, 채널에 연결되어있지않은 유저에게 /user/{userId}로 메시지 발송
  // channel eq, user online true
  public List<Long> fetchUserIdListWhoConnectedButNotSubscribe(Channel channel) {
    return queryFactory
        .select(qChannelUserRelation.user.id)
        .from(qChannelUserRelation)
        .where(channelEq(channel), subscribeFalse(), userOnlineTrue())
        .fetch();
  }

  private BooleanExpression subscribeFalse() {
    return qChannelUserRelation.subscribe.isFalse();
  }

  private BooleanExpression userOnlineTrue() {
    return qChannelUserRelation.user.online.isTrue();
  }

  public List<ChannelUserRelation> fetchChannelUserRelationListBySubscribeTrueAndUser(User user) {
    return queryFactory
        .select(qChannelUserRelation)
        .from(qChannelUserRelation)
        .where(userEq(user), subscribeTrue())
        .fetch();
  }

  private BooleanExpression subscribeTrue() {
    return qChannelUserRelation.subscribe.isTrue();
  }

  // 등록된 상태, 최근 timeout 시간안에 갱신되지않은 상태, online 상태 -> offline 메시지 발송이 되지않은 상태
  public List<UserAndServerAndChannelUserRelationForTimeoutCheckDto> fetchUserAndServerAndChannelUserRelationForTimeoutCheckDto(
      LocalDateTime time) {
    return queryFactory
        .select(new QUserAndServerAndChannelUserRelationForTimeoutCheckDto(
            qChannelUserRelation.user,
            qChannelUserRelation.user.id,
            qChannelUserRelation.channel.server.id,
            qChannelUserRelation
        ))
        .from(qChannelUserRelation)
        .where(logicDeleteFalse(), timeoutTrue(time), userOnlineTrue(), subscribeTrue())
        .fetch();
  }

  private BooleanExpression timeoutTrue(LocalDateTime time) {
    return qUser.lastLogin.before(time);
  }

  public void bulkUpdateUnsubscribe(List<ChannelUserRelation> channelUserRelationList) {
    queryFactory
        .update(qChannelUserRelation)
        .set(qChannelUserRelation.subscribe, false)
        .where(channelIdIn(channelUserRelationList), logicDeleteFalse())
        .execute();
  }

  private BooleanExpression channelIdIn(List<ChannelUserRelation> channelUserRelationList) {
    return qChannelUserRelation.in(channelUserRelationList);
  }

  public void bulkDeleteByServerIdAndEmail(Long serverId, String email) {
    queryFactory
        .delete(qChannelUserRelation)
        .where(serverIdEq(serverId), userEmailEq(email))
        .execute();
  }

  public void bulkDeleteByChannelId(Long channelId) {
    queryFactory
        .delete(qChannelUserRelation)
        .where(channelIdEq(channelId))
        .execute();
  }

  public void bulkInsertServerJoin(List<ChannelRegistrationDto> channelRegistrationDtoList,
      Long userId) {
    String sql = "INSERT INTO channel_user_relation "
        + "(channel_id, user_id, read_message,write_message, view_history, last_read_message_id, subscribe) "
        + "VALUES (?,?,?,?,?,?,?)";

    jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
      @Override
      public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
        Long channelId = channelRegistrationDtoList.get(i).getChannel();
        Long lastMessageId = channelRegistrationDtoList.get(i).getLastMessageId();
        ps.setLong(1, channelId); // channelId
        ps.setLong(2, userId); // userId
        ps.setBoolean(3, true); // readMessage
        ps.setBoolean(4, true); // writeMessage
        ps.setBoolean(5, true); // viewHistory
        if (lastMessageId != null) {
          ps.setLong(6, lastMessageId); // lastReadMessageId
        } else {
          ps.setNull(6, 0); // lastReadMessageId
        }
        ps.setBoolean(7, false); // subscribe
      }

      @Override
      public int getBatchSize() {
        return channelRegistrationDtoList.size();
      }
    });
  }

  public void bulkInsertChannelCreate(Long channelId, List<Long> userIdList) {
    String sql = "INSERT INTO channel_user_relation "
        + "(channel_id, user_id, read_message,write_message, view_history, last_read_message_id, subscribe) "
        + "VALUES (?,?,?,?,?,?,?)";

    jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
      @Override
      public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
        Long userId = userIdList.get(i);
        ps.setLong(1, channelId); // channelId
        ps.setLong(2, userId); // userId
        ps.setBoolean(3, true); // readMessage
        ps.setBoolean(4, true); // writeMessage
        ps.setBoolean(5, true); // viewHistory
        ps.setNull(6, 0); // lastReadMessageId
        ps.setBoolean(7, false); // subscribe
      }

      @Override
      public int getBatchSize() {
        return userIdList.size();
      }
    });
  }
}
