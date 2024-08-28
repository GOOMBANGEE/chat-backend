package com.chat.repository.server;

import com.chat.domain.server.QServer;
import com.chat.domain.server.QServerUserRelation;
import com.chat.domain.server.Server;
import com.chat.domain.user.QUser;
import com.chat.domain.user.User;
import com.chat.dto.server.QServerInfoDto;
import com.chat.dto.server.ServerInfoDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServerUserRelationRepositoryImpl implements ServerUserRelationRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  QServerUserRelation serverUserRelation = QServerUserRelation.serverUserRelation;
  QServer server = QServer.server;
  QUser user = QUser.user;

  @Override
  public Optional<Server> findServerByUserAndServerId(User user, Long serverId) {
    return Optional.ofNullable(queryFactory
        .select(serverUserRelation.server)
        .from(serverUserRelation)
        .join(serverUserRelation.server, server)
        .where(userEq(user), serverIdEq(serverId), serverDeleteFalse())
        .fetchFirst());
  }

  private BooleanExpression userEq(User user) {
    return serverUserRelation.user.eq(user);
  }

  private BooleanExpression serverIdEq(Long serverId) {
    return serverUserRelation.server.id.eq(serverId);
  }

  private BooleanExpression serverDeleteFalse() {
    return serverUserRelation.server.logicDelete.eq(Boolean.FALSE);
  }

  @Override
  public List<ServerInfoDto> fetchServerInfoDtoListByUser(User user) {
    return queryFactory
        .select(new QServerInfoDto(serverUserRelation.server.id,
            serverUserRelation.server.name))
        .from(serverUserRelation)
        .join(serverUserRelation.server, server)
        .where(userEq(user), serverDeleteFalse())
        .fetch();
  }
}

