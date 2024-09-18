package com.chat.repository.category;

import com.chat.domain.user.User;
import com.chat.dto.category.CategoryInfoDto;
import java.util.List;

public interface CategoryUserRelationRepositoryCustom {

  List<CategoryInfoDto> fetchCategoryInfoDtoListByUser(User user);
}
