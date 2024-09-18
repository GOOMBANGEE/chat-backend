package com.chat.repository.channel;

import com.chat.domain.user.User;
import com.chat.dto.channel.ChannelInfoDto;
import java.util.List;

public interface ChannelUserRelationRepositoryCustom {

  List<ChannelInfoDto> fetchChannelInfoDtoListByUser(User user);
}
