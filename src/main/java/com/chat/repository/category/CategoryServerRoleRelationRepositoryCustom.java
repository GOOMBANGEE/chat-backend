package com.chat.repository.category;

import com.chat.domain.server.ServerRole;
import com.chat.dto.category.CategoryInfoDto;
import java.util.List;

public interface CategoryServerRoleRelationRepositoryCustom {

  List<CategoryInfoDto> fetchCategoryInfoDtoListByServerRoleList(
      List<ServerRole> serverRoleList);
}
