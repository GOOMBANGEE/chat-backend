package com.chat.repository.channel;

import static org.springframework.util.ObjectUtils.isEmpty;

import com.chat.domain.channel.Channel;
import com.chat.domain.channel.ChannelUserRelation;
import com.chat.domain.channel.QChannelUserRelation;
import com.chat.domain.server.Server;
import com.chat.domain.user.User;
import com.chat.dto.channel.ChannelInfoDto;
import com.chat.dto.channel.ChannelUserRelationInfoDto;
import com.chat.dto.channel.QChannelInfoDto;
import com.chat.dto.channel.QChannelUserRelationInfoDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChannelUserRelationRepositoryImpl implements ChannelUserRelationRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  QChannelUserRelation qChannelUserRelation = QChannelUserRelation.channelUserRelation;

  @Override
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
          .fetchOne();
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
          .fetchOne();
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

  @Override
  public Optional<ChannelUserRelation> fetchChannelUserRelationByChannelIdAndUserId(Long channelId,
      Long userId) {
    return Optional.ofNullable(queryFactory
        .selectFrom(qChannelUserRelation)
        .where(channelIdEq(channelId), userIdEq(userId))
        .fetchOne());
  }

  private BooleanExpression userIdEq(Long userId) {
    return qChannelUserRelation.user.id.eq(userId);
  }

  @Override
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

  @Override
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

  @Override
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

  @Override
  public List<ChannelUserRelation> fetchChannelUserRelationListByServerAndUser
      (Server server, User user) {
    return queryFactory
        .select(qChannelUserRelation)
        .from(qChannelUserRelation)
        .where(serverEq(server), userEq(user), logicDeleteFalse())
        .fetch();
  }

  private BooleanExpression serverEq(Server server) {
    return isEmpty(server) ? null : qChannelUserRelation.channel.server.eq(server);
  }

  // 두 유저가 속해있는 dm채널이 있는지 확인
  @Override
  public Optional<ChannelUserRelation> searchDirectMessageChannel(User user, User mentionedUser) {
    return Optional.ofNullable(queryFactory
        .select(qChannelUserRelation)
        .from(qChannelUserRelation)
        .where(userEq(user), mentionedUserEq(mentionedUser))
        .fetchOne());
  }

  private BooleanExpression mentionedUserEq(User mentionedUser) {
    return qChannelUserRelation.userDirectMessage.eq(mentionedUser);
  }

  // 접속해있지만, 채널에 연결되어있지않은 유저에게 /user/{userId}로 메시지 발송
  // channel eq, user online true
  @Override
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

  @Override
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
}
