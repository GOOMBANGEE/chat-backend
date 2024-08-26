package com.chat.service.chat;

import com.chat.domain.Chat;
import com.chat.domain.server.Server;
import com.chat.domain.user.User;
import com.chat.dto.MessageDto;
import com.chat.dto.chat.ChatInfoDto;
import com.chat.dto.chat.ChatListResponseDto;
import com.chat.dto.chat.SendMessageResponseDto;
import com.chat.exception.ServerException;
import com.chat.repository.chat.ChatRepository;
import com.chat.repository.server.ServerUserRelationRepository;
import com.chat.repository.user.UserRepository;
import com.chat.service.user.CustomUserDetailsService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatService {

  private final CustomUserDetailsService customUserDetailsService;
  private final UserRepository userRepository;
  private final ServerUserRelationRepository serverUserRelationRepository;
  private final ChatRepository chatRepository;

  private final SimpMessagingTemplate messagingTemplate;

  private static final String USER_UNREGISTERED = "SERVER:USER_UNREGISTERED";
  private static final String SERVER_NOT_FOUND = "SERVER:SERVER_NOT_FOUND";
  private static final String SUB_SERVER = "/sub/server/";

  @Transactional
  public SendMessageResponseDto sendMessage(MessageDto messageDto) {
    String email = customUserDetailsService.getEmailByUserDetails();

    Long serverId = messageDto.getServerId();
    String username = messageDto.getUsername();
    String message = messageDto.getMessage();

    // 해당 서버 참여자인지 확인
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ServerException(USER_UNREGISTERED));

    // todo role check
    // 현재는 참여자확인만 이루어짐
    Server server = serverUserRelationRepository.findServerByUserAndServerId(user, serverId)
        .orElseThrow(() -> new ServerException(SERVER_NOT_FOUND));

    // 메세지 저장
    Chat chat = Chat.builder()
        .message(message)
        .server(server)
        .user(user)
        .username(username)
        .build();
    chatRepository.save(chat);

    // stomp pub
    String serverUrl = SUB_SERVER + serverId;
    MessageDto newMessageDto = chat.buildMessageDtoForSendMessageResponse(messageDto);
    messagingTemplate.convertAndSend(serverUrl, newMessageDto);

    // return
    Long id = chat.fetchChatIdForSendMessageResponse();
    return SendMessageResponseDto.builder()
        .serverId(serverId)
        .id(id)
        .build();
  }

  public ChatListResponseDto list(Long serverId) {
    String email = customUserDetailsService.getEmailByUserDetails();

    // 해당 서버 참여자인지 확인
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ServerException(USER_UNREGISTERED));

    // todo role check
    // 현재는 참여자확인만 이루어짐
    if (serverUserRelationRepository.findServerByUserAndServerId(user, serverId).isEmpty()) {
      throw new ServerException(SERVER_NOT_FOUND);
    }
    
    // 최근 50개 fetch
    List<ChatInfoDto> chatInfoDtoList = chatRepository.fetchChatInfoDtoListByServerId(serverId);

    return ChatListResponseDto.builder()
        .chatList(chatInfoDtoList)
        .build();
  }
}
