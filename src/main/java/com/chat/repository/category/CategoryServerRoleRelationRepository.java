package com.chat.repository.category;

import com.chat.domain.category.CategoryServerRoleRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryServerRoleRelationRepository extends
    JpaRepository<CategoryServerRoleRelation, Long> {
  
  @Modifying
  @Query("DELETE CategoryServerRoleRelation csrr WHERE csrr.category.id =:categoryId")
  void bulkDeleteByCategoryId(Long categoryId);
}
