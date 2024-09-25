package com.chat.repository.category;

import com.chat.domain.server.Server;
import com.chat.dto.category.CategoryInfoDto;
import java.util.List;

public interface CategoryRepositoryCustom {

  List<CategoryInfoDto> fetchCategoryInfoDtoListByServerIdList(List<Long> serverIdList);

  Double fetchMaxDisplayOrder(Server server);

}
