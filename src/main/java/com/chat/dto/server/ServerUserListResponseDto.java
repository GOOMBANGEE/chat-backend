package com.chat.dto.server;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerUserListResponseDto {

  private List<ServerUserInfoDto> serverUserInfoDtoList;

  @Builder
  public ServerUserListResponseDto(List<ServerUserInfoDto> serverUserInfoDtoList) {
    this.serverUserInfoDtoList = serverUserInfoDtoList;
  }
}
