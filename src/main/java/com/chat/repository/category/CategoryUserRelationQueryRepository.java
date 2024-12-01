package com.chat.repository.category;

import static org.springframework.util.ObjectUtils.isEmpty;

import com.chat.domain.category.QCategoryUserRelation;
import com.chat.domain.user.User;
import com.chat.dto.category.CategoryInfoDto;
import com.chat.dto.category.QCategoryInfoDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CategoryUserRelationQueryRepository {

  private final JPAQueryFactory queryFactory;
  private final JdbcTemplate jdbcTemplate;
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

  public void bulkInsertCategoryIdListAndUserId(List<Long> categoryIdList, Long userId) {
    String sql = "INSERT INTO category_user_relation "
        + "(category_id, user_id, read_message, write_message, view_history) "
        + "VALUES (?, ?, ?, ?, ?)";

    jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
      @Override
      public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
        Long categoryId = categoryIdList.get(i);
        ps.setLong(1, categoryId); // categoryId
        ps.setLong(2, userId); // userId
        ps.setBoolean(3, true); // readMessage
        ps.setBoolean(4, true); // writeMessage
        ps.setBoolean(5, true); // viewHistory
      }

      @Override
      public int getBatchSize() {
        return categoryIdList.size();
      }
    });
  }

  public void bulkInsertCategoryIdAndUserIdList(Long categoryId, List<Long> userIdList) {
    String sql = "INSERT INTO category_user_relation "
        + "(category_id, user_id, read_message, write_message, view_history) "
        + "VALUES (?, ?, ?, ?, ?)";

    jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
      @Override
      public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
        Long userId = userIdList.get(i);
        ps.setLong(1, categoryId); // categoryId
        ps.setLong(2, userId); // userId
        ps.setBoolean(3, true); // readMessage
        ps.setBoolean(4, true); // writeMessage
        ps.setBoolean(5, true); // viewHistory
      }

      @Override
      public int getBatchSize() {
        return userIdList.size();
      }
    });
  }
}
