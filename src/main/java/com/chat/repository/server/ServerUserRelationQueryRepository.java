package com.chat.repository.server;

import static org.springframework.util.ObjectUtils.isEmpty;

import com.chat.domain.server.QServer;
import com.chat.domain.server.QServerUserRelation;
import com.chat.domain.server.Server;
import com.chat.domain.server.ServerUserRelation;
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
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ServerUserRelationQueryRepository {

  private final JPAQueryFactory queryFactory;
  QServerUserRelation qServerUserRelation = QServerUserRelation.serverUserRelation;
  QServer qServer = QServer.server;
  QUser qUser = QUser.user;

  public Optional<Server> fetchServerByUserAndServerId(User user, Long serverId) {
    return Optional.ofNullable(queryFactory
        .select(qServerUserRelation.server)
        .from(qServerUserRelation)
        .join(qServerUserRelation.server, qServer)
        .where(userEq(user), serverIdEq(serverId), serverDeleteFalse(), logicDeleteFalse())
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

  private BooleanExpression logicDeleteFalse() {
    return qServerUserRelation.logicDelete.eq(Boolean.FALSE);
  }

  public Optional<ServerUserRelation> fetchServerUserRelationByServerIdAndUserId(Long serverId,
      Long userId) {
    return Optional.ofNullable(queryFactory
        .selectFrom(qServerUserRelation)
        .where(serverIdEq(serverId), userIdEq(userId))
        .fetchFirst());
  }

  private BooleanExpression userIdEq(Long userId) {
    return qServerUserRelation.user.id.eq(userId);
  }

  public List<ServerInfoDto> fetchServerInfoDtoListByUser(User user) {
    return queryFactory
        .select(new QServerInfoDto(
            qServerUserRelation.server.id,
            qServerUserRelation.server.name,
            qServerUserRelation.server.icon))
        .from(qServerUserRelation)
        .join(qServerUserRelation.server, qServer)
        .where(userEq(user), serverDeleteFalse(), logicDeleteFalse())
        .fetch();
  }

  // 서버에 속해있는 유저의 정보
  public List<ServerUserInfoDto> fetchServerUserInfoDtoListByServer(Server server) {
    return queryFactory
        .select(
            new QServerUserInfoDto(
                qServerUserRelation.user.id,
                qServerUserRelation.user.username,
                qServerUserRelation.user.avatarImageSmall,
                qServerUserRelation.user.online))
        .from(qServerUserRelation)
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

  // 유저가 속해있는 모든 서버의 id 가져옴
  // server.id
  public List<Long> fetchServerIdListByUserAndServerDeleteFalseAndLogicDeleteFalse(
      User user) {
    return queryFactory
        .select(qServerUserRelation.server.id)
        .from(qServerUserRelation)
        .where(userEq(user), serverDeleteFalse(), logicDeleteFalse())
        .fetch();
  }

  // server참가자 id list
  public List<Long> fetchUserIdListByServer(Server server) {
    return queryFactory
        .select(qServerUserRelation.user.id)
        .from(qServerUserRelation)
        .where(serverEq(server), serverDeleteFalse(), logicDeleteFalse())
        .fetch();
  }
}
