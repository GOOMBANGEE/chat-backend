package com.chat.repository.category;

import com.chat.domain.category.Category;
import com.chat.domain.category.CategoryUserRelation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryUserRelationRepository extends JpaRepository<CategoryUserRelation, Long> {

  List<CategoryUserRelation> findByCategory(Category category);
}
