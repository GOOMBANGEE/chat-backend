package com.chat.repository.server;

import com.chat.domain.server.Server;
import com.chat.domain.server.ServerRole;
import com.chat.domain.user.User;
import java.util.List;

public interface ServerRoleUserRelationRepositoryCustom {

  // 유저가 가지고있는 권한리스트 확인
  // 채널 생성 권한 확인
  // server, user가 주어졌을때, ServerRole에서 createChannel이 활성화 되어있는지 확인
  List<ServerRole> fetchServerRoleListByServerAndUser(Server server, User user);

  List<ServerRole> fetchServerRoleListByUser(User user);
}
