package com.chat.repository.server;

import com.chat.domain.server.Server;
import com.chat.domain.user.User;
import com.chat.dto.server.ServerInfoDto;
import com.chat.dto.server.ServerUserInfoDto;
import java.util.List;
import java.util.Optional;

public interface ServerUserRelationRepositoryCustom {

  Optional<Server> findServerByUserAndServerId(User user, Long serverId);

  List<ServerInfoDto> fetchServerInfoDtoListByUser(User user);

  List<ServerUserInfoDto> fetchServerUserInfoDtoListByUserAndServer(User user,
      Server server);

  // 유저가 속해있는 모든 서버의 id 가져옴
  // server.id
  List<Long> fetchServerInfoDtoListByUserAndServerAndLogicDeleteFalse(
      User user);
}
