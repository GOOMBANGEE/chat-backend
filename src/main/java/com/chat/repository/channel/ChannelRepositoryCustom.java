package com.chat.repository.channel;

import com.chat.domain.category.Category;
import com.chat.domain.server.Server;
import com.chat.dto.MessageQueueInitializeDto;
import com.chat.dto.channel.ChannelRegistrationDto;
import java.util.List;

public interface ChannelRepositoryCustom {

  List<ChannelRegistrationDto> fetchChannelRegistrationDtoListByServer(Server server);

  List<MessageQueueInitializeDto> fetchMessageQueueInitializeDtoList();

  Double fetchMaxDisplayOrderByCategory(Category category);

  Double fetchMaxDisplayOrderByServerAndCategoryNull(Server server);
}
