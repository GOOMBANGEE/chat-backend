package com.chat.repository.channel;

import com.chat.domain.channel.Channel;
import com.chat.domain.channel.ChannelUserRelation;
import com.chat.domain.server.Server;
import com.chat.domain.user.User;
import com.chat.dto.channel.ChannelInfoDto;
import com.chat.dto.channel.ChannelUserRelationInfoDto;
import java.util.List;
import java.util.Optional;

public interface ChannelUserRelationRepositoryCustom {

  ChannelUserRelationInfoDto fetchChannelUserRelationInfoDtoByServerIdAndChannelIdAndUserEmail(
      Long serverId, Long channelId, String email);

  Optional<ChannelUserRelation> fetchChannelUserRelationByChannelIdAndUserId(Long channelId,
      Long userId);

  List<ChannelInfoDto> fetchChannelInfoDtoListByUser(User user);

  List<User> fetchUserListByChannel(Channel channel);

  List<ChannelUserRelation> fetchChannelUserRelationListByServerAndUser
      (Server server, User user);
}
