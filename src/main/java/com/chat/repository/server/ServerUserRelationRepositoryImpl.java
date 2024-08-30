package com.chat.repository.server;

import static org.springframework.util.ObjectUtils.isEmpty;

import com.chat.domain.server.QServer;
import com.chat.domain.server.QServerUserRelation;
import com.chat.domain.server.Server;
import com.chat.domain.user.QUser;
import com.chat.domain.user.User;
import com.chat.dto.server.QServerInfoDto;
import com.chat.dto.server.QServerUserInfoDto;
import com.chat.dto.server.ServerInfoDto;
import com.chat.dto.server.ServerUserInfoDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServerUserRelationRepositoryImpl implements ServerUserRelationRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  QServerUserRelation qServerUserRelation = QServerUserRelation.serverUserRelation;
  QServer qServer = QServer.server;
  QUser qUser = QUser.user;

  @Override
  public Optional<Server> findServerByUserAndServerId(User user, Long serverId) {
    return Optional.ofNullable(queryFactory
        .select(qServerUserRelation.server)
        .from(qServerUserRelation)
        .join(qServerUserRelation.server, qServer)
        .where(userEq(user), serverIdEq(serverId), serverDeleteFalse())
        .fetchFirst());
  }

  private BooleanExpression userEq(User user) {
    return qServerUserRelation.user.eq(user);
  }

  private BooleanExpression serverIdEq(Long serverId) {
    return qServerUserRelation.server.id.eq(serverId);
  }

  private BooleanExpression serverDeleteFalse() {
    return qServerUserRelation.server.logicDelete.eq(Boolean.FALSE);
  }

  @Override
  public List<ServerInfoDto> fetchServerInfoDtoListByUser(User user) {
    return queryFactory
        .select(new QServerInfoDto(qServerUserRelation.server.id,
            qServerUserRelation.server.name))
        .from(qServerUserRelation)
        .join(qServerUserRelation.server, qServer)
        .where(userEq(user), serverDeleteFalse())
        .fetch();
  }

  @Override
  public List<ServerUserInfoDto> fetchServerUserInfoDtoListByUserAndServer(User user,
      Server server) {
    return queryFactory
        .select(
            new QServerUserInfoDto(qServerUserRelation.user.id,
                qServerUserRelation.user.username))
        .from(qServerUserRelation)
        .join(qServerUserRelation.server, qServer)
        .join(qServerUserRelation.user, qUser)
        .where(serverEq(server), userDeleteFalse())
        .fetch();
  }

  private BooleanExpression serverEq(Server server) {
    return isEmpty(server) ? null : qServerUserRelation.server.eq(server);
  }

  private BooleanExpression userDeleteFalse() {
    return qServerUserRelation.user.logicDelete.eq(Boolean.FALSE);
  }
}

