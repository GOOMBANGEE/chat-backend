package com.chat.repository.channel;

import com.chat.domain.channel.QChannelServerRoleRelation;
import com.chat.domain.server.ServerRole;
import com.chat.dto.channel.ChannelInfoDto;
import com.chat.dto.channel.QChannelInfoDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChannelServerRoleRelationRepositoryImpl implements
    ChannelServerRoleRelationRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  QChannelServerRoleRelation qChannelServerRoleRelation = QChannelServerRoleRelation.channelServerRoleRelation;

  @Override
  public List<ChannelInfoDto> fetchChannelInfoDtoListByServerRoleList(
      List<ServerRole> serverRoleList) {
    return queryFactory
        .select(new QChannelInfoDto(
            qChannelServerRoleRelation.channel.id,
            qChannelServerRoleRelation.channel.name,
            qChannelServerRoleRelation.channel.displayOrder,
            qChannelServerRoleRelation.channel.server.id,
            qChannelServerRoleRelation.channel.category.id))
        .from(qChannelServerRoleRelation)
        .where(serverRoleIn(serverRoleList), logicDeleteFalse())
        .fetch();
  }

  private BooleanExpression serverRoleIn(List<ServerRole> serverRoleList) {
    return qChannelServerRoleRelation.serverRole.in(serverRoleList);
  }

  private BooleanExpression logicDeleteFalse() {
    return qChannelServerRoleRelation.channel.logicDelete.isFalse();
  }
}
