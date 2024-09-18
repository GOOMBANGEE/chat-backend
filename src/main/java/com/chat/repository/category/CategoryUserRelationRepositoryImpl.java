package com.chat.repository.category;

import static org.springframework.util.ObjectUtils.isEmpty;

import com.chat.domain.category.QCategoryUserRelation;
import com.chat.domain.user.User;
import com.chat.dto.category.CategoryInfoDto;
import com.chat.dto.category.QCategoryInfoDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CategoryUserRelationRepositoryImpl implements CategoryUserRelationRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  QCategoryUserRelation qCategoryUserRelation = QCategoryUserRelation.categoryUserRelation;

  @Override
  public List<CategoryInfoDto> fetchCategoryInfoDtoListByUser(User user) {
    return queryFactory
        .select(new QCategoryInfoDto(
            qCategoryUserRelation.category.id,
            qCategoryUserRelation.category.name,
            qCategoryUserRelation.category.displayOrder,
            qCategoryUserRelation.category.server.id))
        .from(qCategoryUserRelation)
        .where(userEq(user), logicDeleteFalse())
        .fetch();
  }

  private BooleanExpression userEq(User user) {
    return isEmpty(user) ? null : qCategoryUserRelation.user.eq(user);
  }

  private BooleanExpression logicDeleteFalse() {
    return qCategoryUserRelation.category.logicDelete.isFalse();
  }
}
