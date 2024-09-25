package com.chat.repository.category;

import static org.springframework.util.ObjectUtils.isEmpty;

import com.chat.domain.category.QCategory;
import com.chat.domain.server.Server;
import com.chat.dto.category.CategoryInfoDto;
import com.chat.dto.category.QCategoryInfoDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  QCategory qCategory = QCategory.category;

  @Override
  public List<CategoryInfoDto> fetchCategoryInfoDtoListByServerIdList(List<Long> serverIdList) {
    return queryFactory
        .select(new QCategoryInfoDto(
            qCategory.id,
            qCategory.name,
            qCategory.displayOrder,
            qCategory.server.id))
        .from(qCategory)
        .where(serverIdIn(serverIdList), logicDeleteFalse())
        .fetch();
  }

  private BooleanExpression serverIdIn(List<Long> serverIdList) {
    return qCategory.server.id.in(serverIdList);
  }

  private BooleanExpression logicDeleteFalse() {
    return qCategory.logicDelete.isFalse();
  }

  @Override
  public Double fetchMaxDisplayOrder(Server server) {
    return queryFactory
        .select(qCategory.displayOrder.max().coalesce(1024.0))
        .from(qCategory)
        .where(serverEq(server))
        .fetchFirst();
  }

  private BooleanExpression serverEq(Server server) {
    return isEmpty(server) ? null : qCategory.server.eq(server);
  }
}
