package com.chat.repository.server;

import com.chat.dto.server.ServerJoinInfoDto;

public interface ServerRepositoryCustom {

  ServerJoinInfoDto fetchServerInfoDtoByServerCode(String code);

}
