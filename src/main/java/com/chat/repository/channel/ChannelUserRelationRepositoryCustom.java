package com.chat.repository.channel;

import com.chat.domain.channel.Channel;
import com.chat.domain.channel.ChannelUserRelation;
import com.chat.domain.server.Server;
import com.chat.domain.user.User;
import com.chat.dto.channel.ChannelInfoDto;
import com.chat.dto.channel.ChannelUserRelationInfoDto;
import java.util.List;

public interface ChannelUserRelationRepositoryCustom {

  ChannelUserRelationInfoDto fetchChannelUserRelationInfoDtoByServerIdAndChannelIdAndUserEmail(
      Long serverId, Long channelId, String email);

  List<ChannelInfoDto> fetchChannelInfoDtoListByUser(User user);

  List<User> fetchUserListByChannel(Channel channel);

  List<ChannelUserRelation> fetchChannelUserRelationListByServerAndUser
      (Server server, User user);
}
