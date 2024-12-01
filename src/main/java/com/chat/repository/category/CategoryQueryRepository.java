package com.chat.repository.category;

import static org.springframework.util.ObjectUtils.isEmpty;

import com.chat.domain.category.QCategory;
import com.chat.domain.server.Server;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CategoryQueryRepository {

  private final JPAQueryFactory queryFactory;
  QCategory qCategory = QCategory.category;

  public Double fetchMaxDisplayOrder(Server server) {
    return queryFactory
        .select(qCategory.displayOrder.max().coalesce(1024.0))
        .from(qCategory)
        .where(serverEq(server), logicDeleteFalse())
        .fetchFirst();
  }

  private BooleanExpression logicDeleteFalse() {
    return qCategory.logicDelete.isFalse();
  }

  private BooleanExpression serverEq(Server server) {
    return isEmpty(server) ? null : qCategory.server.eq(server);
  }

  public List<Long> fetchIdListByServer(Long serverId) {
    return queryFactory
        .select(qCategory.id)
        .from(qCategory)
        .where(serverIdEq(serverId), logicDeleteFalse())
        .fetch();
  }

  private BooleanExpression serverIdEq(Long serverId) {
    return qCategory.server.id.eq(serverId);
  }
}
