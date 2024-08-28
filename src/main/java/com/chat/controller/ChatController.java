package com.chat.controller;

import com.chat.dto.EmptyResponseDto;
import com.chat.dto.MessageDto;
import com.chat.dto.chat.ChatListResponseDto;
import com.chat.dto.chat.SendMessageResponseDto;
import com.chat.service.chat.ChatService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;

  private static final String SERVER_INVALID = "VALID:SERVER_INVALID";
  private static final String CHAT_INVALID = "VALID:CHAT_INVALID";

  @PostMapping("")
  public ResponseEntity<SendMessageResponseDto> sendMessage(@RequestBody MessageDto messageDto) {
    SendMessageResponseDto responseDto = chatService.sendMessage(messageDto);
    return ResponseEntity.ok(responseDto);
  }

  @PatchMapping("")
  public ResponseEntity<EmptyResponseDto> updateMessage(@RequestBody MessageDto messageDto) {
    chatService.updateMessage(messageDto);
    return ResponseEntity.ok(null);
  }

  @GetMapping("/list/{serverId}")
  public ResponseEntity<ChatListResponseDto> list(
      @NotNull(message = SERVER_INVALID)
      @PathVariable("serverId")
      Long serverId) {
    ChatListResponseDto responseDto = chatService.list(serverId);
    return ResponseEntity.ok(responseDto);
  }

  @DeleteMapping("/{serverId}/{chatId}")
  public ResponseEntity<EmptyResponseDto> delete(
      @NotNull(message = SERVER_INVALID)
      @PathVariable("serverId")
      Long serverId,
      @NotNull(message = CHAT_INVALID)
      @PathVariable("chatId")
      Long chatId
  ) {
    chatService.delete(serverId, chatId);
    return ResponseEntity.ok(null);
  }

}
