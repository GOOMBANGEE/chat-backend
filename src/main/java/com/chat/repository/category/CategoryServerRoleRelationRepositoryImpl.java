package com.chat.repository.category;

import com.chat.domain.category.QCategoryServerRoleRelation;
import com.chat.domain.server.ServerRole;
import com.chat.dto.category.CategoryInfoDto;
import com.chat.dto.category.QCategoryInfoDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CategoryServerRoleRelationRepositoryImpl implements
    CategoryServerRoleRelationRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  QCategoryServerRoleRelation qCategoryServerRoleRelation = QCategoryServerRoleRelation.categoryServerRoleRelation;

  @Override
  public List<CategoryInfoDto> fetchCategoryInfoDtoListByServerRoleList(
      List<ServerRole> serverRoleList) {
    return queryFactory
        .select(new QCategoryInfoDto(
            qCategoryServerRoleRelation.category.id,
            qCategoryServerRoleRelation.category.name,
            qCategoryServerRoleRelation.category.displayOrder,
            qCategoryServerRoleRelation.category.server.id))
        .from(qCategoryServerRoleRelation)
        .where(serverRoleIn(serverRoleList), logicDeleteFalse())
        .fetch();
  }

  private BooleanExpression serverRoleIn(List<ServerRole> serverRoleList) {
    return qCategoryServerRoleRelation.serverRole.in(serverRoleList);
  }

  private BooleanExpression logicDeleteFalse() {
    return qCategoryServerRoleRelation.category.logicDelete.isFalse();
  }
}
