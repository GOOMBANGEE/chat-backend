package com.chat.repository.channel;

import com.chat.domain.channel.Channel;
import com.chat.domain.channel.ChannelUserRelation;
import com.chat.domain.server.Server;
import com.chat.domain.user.User;
import com.chat.dto.channel.ChannelInfoDto;
import java.util.List;

public interface ChannelUserRelationRepositoryCustom {

  List<ChannelInfoDto> fetchChannelInfoDtoListByUser(User user);

  List<User> fetchUserListByChannel(Channel channel);

  List<ChannelUserRelation> fetchChannelUserRelationListByServerAndUser
      (Server server, User user);
}
