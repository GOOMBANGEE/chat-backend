package com.chat.repository.chat;

import com.chat.domain.channel.Channel;
import com.chat.dto.chat.ChatInfoDto;
import com.chat.dto.chat.ChatReferenceInfoForSendMessageResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatRepositoryCustom {


  List<ChatInfoDto> fetchChatInfoDtoListByChannelId(Long channelId);

  List<ChatInfoDto> fetchChatInfoDtoListByChannelIdAndChatId(Long channelId, Long chatId);


  Page<ChatInfoDto> searchChatInfoDtoListDefault(Channel channel, String keyword,
      Pageable pageable);

  Page<ChatInfoDto> searchChatInfoDtoListByUsername(Channel channel, String username,
      Pageable pageable);

  Page<ChatInfoDto> searchChatInfoDtoListByMessage(Channel channel, String message,
      Pageable pageable);

  Page<ChatInfoDto> searchChatInfoDtoListByUsernameAndMessage(Channel channel, String username,
      String message, Pageable pageable);

  ChatReferenceInfoForSendMessageResponse fetchChatReferenceInfoForSendMessageResponseByChatIdAndChannel(
      Long chatId, Channel channel);
}
