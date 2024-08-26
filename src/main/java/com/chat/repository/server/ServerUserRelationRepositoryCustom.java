package com.chat.repository.server;

import com.chat.domain.server.Server;
import com.chat.domain.user.User;
import com.chat.dto.server.ServerInfoDto;
import java.util.List;
import java.util.Optional;

public interface ServerUserRelationRepositoryCustom {

  Optional<Server> findServerByUserAndServerId(User user, Long serverId);

  List<ServerInfoDto> fetchServerInfoDtoListByUser(User user);
}
