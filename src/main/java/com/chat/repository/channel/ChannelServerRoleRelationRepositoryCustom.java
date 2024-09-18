package com.chat.repository.channel;

import com.chat.domain.server.ServerRole;
import com.chat.dto.channel.ChannelInfoDto;
import java.util.List;

public interface ChannelServerRoleRelationRepositoryCustom {

  List<ChannelInfoDto> fetchChannelInfoDtoListByServerRoleList(
      List<ServerRole> serverRoleList);
}
