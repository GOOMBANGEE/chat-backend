package com.chat.repository.channel;

import static org.springframework.util.ObjectUtils.isEmpty;

import com.chat.domain.channel.Channel;
import com.chat.domain.channel.ChannelUserRelation;
import com.chat.domain.channel.QChannelUserRelation;
import com.chat.domain.server.Server;
import com.chat.domain.user.User;
import com.chat.dto.channel.ChannelInfoDto;
import com.chat.dto.channel.QChannelInfoDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChannelUserRelationRepositoryImpl implements ChannelUserRelationRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  QChannelUserRelation qChannelUserRelation = QChannelUserRelation.channelUserRelation;

  @Override
  public List<ChannelInfoDto> fetchChannelInfoDtoListByUser(User user) {
    return queryFactory
        .select(new QChannelInfoDto(qChannelUserRelation.channel.id,
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
}
