package com.chat.repository.category;

import com.chat.domain.category.CategoryUserRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryUserRelationRepository extends JpaRepository<CategoryUserRelation, Long> {

}
