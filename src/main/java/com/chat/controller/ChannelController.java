package com.chat.controller;

import com.chat.dto.EmptyResponseDto;
import com.chat.dto.channel.ChannelCreateRequestDto;
import com.chat.dto.channel.ChannelCreateResponseDto;
import com.chat.dto.channel.ChannelRenameRequestDto;
import com.chat.dto.channel.ChannelSettingRequestDto;
import com.chat.service.channel.ChannelService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/channel")
@RequiredArgsConstructor
public class ChannelController {

  private final ChannelService channelService;

  private static final String SERVER_INVALID = "VALID:SERVER_INVALID";
  private static final String CHANNEL_INVALID = "VALID:CHANNEL_INVALID";
  private static final String CHAT_INVALID = "VALID:CHAT_INVALID";

  // 채널 생성
  @PostMapping("/create")
  public ResponseEntity<ChannelCreateResponseDto> create(
      @RequestBody @Valid ChannelCreateRequestDto requestDto) throws JsonProcessingException {
    ChannelCreateResponseDto responseDto = channelService.create(requestDto);

    return ResponseEntity.ok(responseDto);
  }

  // 채널 이름변경
  @PostMapping("/{serverId}/{channelId}/rename")
  public ResponseEntity<EmptyResponseDto> rename(
      @NotNull(message = SERVER_INVALID)
      @PathVariable("serverId")
      Long serverId,
      @NotNull(message = CHANNEL_INVALID)
      @PathVariable("channelId")
      Long channelId,
      @RequestBody @Valid ChannelRenameRequestDto requestDto) throws JsonProcessingException {
    channelService.rename(serverId, channelId, requestDto);
    return ResponseEntity.ok(null);
  }


  // 채널 설정변경
  @PostMapping("/{serverId}/{channelId}/setting")
  public ResponseEntity<EmptyResponseDto> setting(
      @NotNull(message = SERVER_INVALID)
      @PathVariable("serverId")
      Long serverId,
      @NotNull(message = CHANNEL_INVALID)
      @PathVariable("channelId")
      Long channelId,
      @RequestBody @Valid ChannelSettingRequestDto requestDto) throws JsonProcessingException {
    channelService.setting(serverId, channelId, requestDto);
    return ResponseEntity.ok(null);
  }

  // 채널 삭제
  @PostMapping("/{serverId}/{channelId}/delete")
  public ResponseEntity<EmptyResponseDto> delete(
      @NotNull(message = SERVER_INVALID)
      @PathVariable("serverId")
      Long serverId,
      @NotNull(message = CHANNEL_INVALID)
      @PathVariable("channelId")
      Long channelId) {
    channelService.delete(serverId, channelId);
    return ResponseEntity.ok(null);
  }
//
//  // 채널 유저 목록
//  @GetMapping("/{channelId}/list/user")
//  public ResponseEntity<ServerUserListResponseDto> userList(
//      @NotNull(message = CHANNEL_INVALID)
//      @PathVariable("channelId")
//      Long channelId) {
//    ServerUserListResponseDto responseDto = channelService.userList(channelId);
//    return ResponseEntity.ok(responseDto);
//  }

  // 채널 메시지 읽기처리
  @PostMapping("/{channelId}/{chatId}/read")
  public ResponseEntity<EmptyResponseDto> read(
      @NotNull(message = CHANNEL_INVALID)
      @PathVariable("channelId")
      Long channelId,
      @NotNull(message = CHAT_INVALID)
      @PathVariable("chatId")
      Long chatId) {
    channelService.read(channelId, chatId);
    return ResponseEntity.ok(null);
  }
}
