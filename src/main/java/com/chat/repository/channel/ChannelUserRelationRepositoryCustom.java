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

  List<ChannelInfoDto> fetchDirectMessageChannelInfoDtoListByUser(User user);

  List<User> fetchUserListByChannel(Channel channel);

  List<ChannelUserRelation> fetchChannelUserRelationListByServerAndUser
      (Server server, User user);

  // 두 유저가 속해있는 dm채널이 있는지 확인
  Optional<ChannelUserRelation> searchDirectMessageChannel(User user, User mentionedUser);

  // 접속해있지만, 채널에 연결되어있지않은 유저에게 /user/{userId}로 메시지 발송
  // channel eq, user online true
  List<Long> fetchUserIdListWhoConnectedButNotSubscribe(Channel channel);

  List<ChannelUserRelation> fetchChannelUserRelationListBySubscribeTrueAndUser(User user);
}
