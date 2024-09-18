package com.chat.repository.server;

import static org.springframework.util.ObjectUtils.isEmpty;

import com.chat.domain.server.QServerRoleUserRelation;
import com.chat.domain.server.Server;
import com.chat.domain.server.ServerRole;
import com.chat.domain.user.User;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServerRoleUserRelationRepositoryImpl implements
    ServerRoleUserRelationRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  QServerRoleUserRelation qServerRoleUserRelation = QServerRoleUserRelation.serverRoleUserRelation;

  // 유저가 가지고있는 권한리스트 확인
  // 채널 생성 권한 확인
  // server, user가 주어졌을때, ServerRole에서 createChannel이 활성화 되어있는지 확인
  @Override
  public List<ServerRole> fetchServerRoleListByServerAndUser(Server server, User user) {
    return queryFactory
        .select(qServerRoleUserRelation.serverRole)
        .from(qServerRoleUserRelation)
        .where(serverEq(server), userEq(user))
        .fetch();
  }

  private BooleanExpression serverEq(Server server) {
    return isEmpty(server) ? null : qServerRoleUserRelation.serverRole.server.eq(server);
  }

  private BooleanExpression userEq(User user) {
    return isEmpty(user) ? null : qServerRoleUserRelation.user.eq(user);
  }

  @Override
  public List<ServerRole> fetchServerRoleListByUser(User user) {
    return queryFactory
        .select(qServerRoleUserRelation.serverRole)
        .from(qServerRoleUserRelation)
        .where(userEq(user))
        .fetch();
  }
}
