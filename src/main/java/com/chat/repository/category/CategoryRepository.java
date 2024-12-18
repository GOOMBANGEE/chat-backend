package com.chat.repository.category;

import com.chat.domain.category.Category;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  Optional<Category> findByIdAndLogicDeleteFalse(Long id);

  Optional<Category> findByIdAndLogicDeleteFalseAndServerId(Long categoryId, Long serverId);

}
