package com.chat.repository.chat;

import com.chat.dto.chat.ChatInfoDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatRepositoryCustom {

  List<ChatInfoDto> fetchChatInfoDtoListByServerId(Long serverId);

  Page<ChatInfoDto> searchChatInfoDtoListDefault(Long serverId, String keyword, Pageable pageable);

  Page<ChatInfoDto> searchChatInfoDtoListByUsername(Long serverId, String keyword,
      Pageable pageable);

  Page<ChatInfoDto> searchChatInfoDtoListByMessage(Long serverId, String message,
      Pageable pageable);

  Page<ChatInfoDto> searchChatInfoDtoListByUsernameAndMessage(Long serverId, String username,
      String message, Pageable pageable);
}
