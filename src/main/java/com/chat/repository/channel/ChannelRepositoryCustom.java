package com.chat.repository.channel;

import com.chat.dto.channel.ChannelInfoDto;
import java.util.List;

public interface ChannelRepositoryCustom {

  List<ChannelInfoDto> fetchChannelInfoDtoListByServerIdList(List<Long> serverIdList);
}
