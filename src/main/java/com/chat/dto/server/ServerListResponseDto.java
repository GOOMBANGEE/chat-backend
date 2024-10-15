package com.chat.dto.server;

import com.chat.dto.category.CategoryInfoDto;
import com.chat.dto.channel.ChannelInfoDto;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerListResponseDto {

  private List<ServerInfoDto> serverList;

  private List<CategoryInfoDto> categoryList;

  private List<ChannelInfoDto> channelList;

  private List<ChannelInfoDto> directMessageChannelList;

  @Builder
  public ServerListResponseDto(List<ServerInfoDto> serverList, List<CategoryInfoDto> categoryList,
      List<ChannelInfoDto> channelList, List<ChannelInfoDto> directMessageChannelList) {
    this.serverList = serverList;
    this.categoryList = categoryList;
    this.channelList = channelList;
    this.directMessageChannelList = directMessageChannelList;
  }
}
