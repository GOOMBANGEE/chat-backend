package com.chat.repository.server;

import static org.springframework.util.ObjectUtils.isEmpty;

import com.chat.domain.server.QServer;
import com.chat.dto.server.QServerJoinInfoDto;
import com.chat.dto.server.ServerJoinInfoDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ServerQueryRepository {

  private final JPAQueryFactory queryFactory;
  QServer qServer = QServer.server;

  public ServerJoinInfoDto fetchServerInfoDtoByServerCode(String code) {
    return queryFactory
        .select(new QServerJoinInfoDto(qServer, qServer.defaultChannel, qServer.defaultChannel.id))
        .from(qServer)
        .where(codeEq(code), logicDeleteFalse())
        .fetchOne();
  }

  private BooleanExpression codeEq(String code) {
    return isEmpty(code) ? null : qServer.code.eq(code);
  }

  private BooleanExpression logicDeleteFalse() {
    return qServer.logicDelete.isFalse();
  }
}
