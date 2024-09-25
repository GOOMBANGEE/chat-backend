package com.chat.repository.category;

import com.chat.domain.category.Category;
import com.chat.domain.category.CategoryServerRoleRelation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryServerRoleRelationRepository extends
    JpaRepository<CategoryServerRoleRelation, Long>, CategoryServerRoleRelationRepositoryCustom {

  List<CategoryServerRoleRelation> findByCategory(Category category);
}
