package com.chat.repository.server;

import com.chat.domain.server.Server;
import com.chat.domain.server.ServerUserRelation;
import com.chat.domain.user.User;
import com.chat.dto.server.ServerInfoDto;
import com.chat.dto.server.ServerUserInfoDto;
import java.util.List;
import java.util.Optional;

public interface ServerUserRelationRepositoryCustom {

  Optional<Server> fetchServerByUserAndServerId(User user, Long serverId);

  Optional<ServerUserRelation> fetchServerUserRelationByServerIdAndUserId(Long serverId,
      Long userId);

  List<ServerInfoDto> fetchServerInfoDtoListByUser(User user);

  List<ServerUserInfoDto> fetchServerUserInfoDtoListByUserAndServer(User user,
      Server server);

  // 유저가 속해있는 모든 서버의 id 가져옴
  // server.id
  List<Long> fetchServerIdListByUserAndServerDeleteFalseAndLogicDeleteFalse(
      User user);

  List<User> fetchUserListByServer(Server server);

  List<Long> fetchUserIdListByServerAndServerDeleteFalseAndLogicDeleteFalse(Server server);
}
