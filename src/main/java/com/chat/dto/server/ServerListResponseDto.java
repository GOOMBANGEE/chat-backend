package com.chat.dto.server;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerListResponseDto {

  private List<ServerInfoDto> serverList;

  @Builder
  public ServerListResponseDto(List<ServerInfoDto> serverList) {
    this.serverList = serverList;
  }
}
