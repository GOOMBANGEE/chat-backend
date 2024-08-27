package com.chat.controller;

import com.chat.dto.MessageDto;
import com.chat.dto.chat.ChatListResponseDto;
import com.chat.dto.chat.SendMessageResponseDto;
import com.chat.service.chat.ChatService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

  @PostMapping("")
  public ResponseEntity<SendMessageResponseDto> sendMessage(@RequestBody MessageDto messageDto) {
    SendMessageResponseDto responseDto = chatService.sendMessage(messageDto);
    return ResponseEntity.ok(responseDto);
  }

  @GetMapping("/list/{serverId}")
  public ResponseEntity<ChatListResponseDto> list(
      @NotNull(message = SERVER_INVALID)
      @PathVariable("serverId")
      Long serverId) {
    ChatListResponseDto responseDto = chatService.list(serverId);
    return ResponseEntity.ok(responseDto);
  }
}
