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
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CategoryUserRelationQueryRepository {

  private final JPAQueryFactory queryFactory;
  QCategoryUserRelation qCategoryUserRelation = QCategoryUserRelation.categoryUserRelation;

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

  public void bulkDeleteByCategoryId(Long categoryId) {
    queryFactory
        .delete(qCategoryUserRelation)
        .where(categoryIdEq(categoryId))
        .execute();
  }

  private BooleanExpression categoryIdEq(Long categoryId) {
    return qCategoryUserRelation.category.id.eq(categoryId);
  }

  public void bulkDeleteByServerIdAndEmail(Long serverId, String email) {
    queryFactory
        .delete(qCategoryUserRelation)
        .where(serverIdEq(serverId), userEmailEq(email))
        .execute();
  }

  private BooleanExpression serverIdEq(Long serverId) {
    return qCategoryUserRelation.category.server.id.eq(serverId);
  }

  private BooleanExpression userEmailEq(String email) {
    return qCategoryUserRelation.user.email.eq(email);
  }
}
