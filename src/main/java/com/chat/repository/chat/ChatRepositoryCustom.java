package com.chat.repository.chat;

import com.chat.dto.chat.ChatInfoDto;
import java.util.List;

public interface ChatRepositoryCustom {

  List<ChatInfoDto> fetchChatInfoDtoListByServerId(Long serverId);
  
}
