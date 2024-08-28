package com.chat.controller;

import com.chat.dto.EmptyResponseDto;
import com.chat.dto.server.ServerCreateRequestDto;
import com.chat.dto.server.ServerCreateResponseDto;
import com.chat.dto.server.ServerDeleteRequestDto;
import com.chat.dto.server.ServerInviteInfoResponseDto;
import com.chat.dto.server.ServerInviteResponseDto;
import com.chat.dto.server.ServerJoinResponseDto;
import com.chat.dto.server.ServerListResponseDto;
import com.chat.dto.server.ServerSettingRequestDto;
import com.chat.service.server.ServerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
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
@RequestMapping("/api/server")
@RequiredArgsConstructor
public class ServerController {

  private final ServerService serverService;

  private static final String SERVER_INVALID = "VALID:SERVER_INVALID";

  // 서버 생성
  @PostMapping("/create")
  public ResponseEntity<ServerCreateResponseDto> create(
      @RequestBody @Valid ServerCreateRequestDto requestDto) {
    ServerCreateResponseDto responseDto = serverService.create(requestDto);
    return ResponseEntity.ok(responseDto);
  }

  // 참여중인 서버 목록
  @GetMapping("/list")
  public ResponseEntity<ServerListResponseDto> list() {
    ServerListResponseDto responseDto = serverService.list();
    return ResponseEntity.ok(responseDto);
  }

  // 서버 설정변경
  @PostMapping("/setting/{serverId}")
  public ResponseEntity<EmptyResponseDto> setting(
      @NotNull(message = SERVER_INVALID)
      @PathVariable("serverId")
      Long serverId,
      @RequestBody @Valid ServerSettingRequestDto requestDto) throws JsonProcessingException {
    serverService.setting(serverId, requestDto);
    return ResponseEntity.ok(null);
  }


  // 서버 입장
  @PostMapping("/join/{code}")
  public ResponseEntity<ServerJoinResponseDto> join(
      @NotNull(message = SERVER_INVALID)
      @PathVariable("code")
      String code) {
    ServerJoinResponseDto responseDto = serverService.join(code);
    return ResponseEntity.ok(responseDto);
  }

  // 서버 초대코드 조회
  @GetMapping("/invite/{code}")
  public ResponseEntity<ServerInviteInfoResponseDto> inviteInfo(
      @NotNull(message = SERVER_INVALID)
      @PathVariable("code")
      String code) {
    ServerInviteInfoResponseDto responseDto = serverService.inviteInfo(code);
    return ResponseEntity.ok(responseDto);
  }

  // 서버 초대코드 생성
  @PostMapping("/invite/{serverId}")
  public ResponseEntity<ServerInviteResponseDto> invite(
      @NotNull(message = SERVER_INVALID)
      @PathVariable("serverId")
      Long serverId) {
    ServerInviteResponseDto responseDto = serverService.invite(serverId);
    return ResponseEntity.ok(responseDto);
  }

  // 서버 삭제
  @PostMapping("/delete/{serverId}")
  public ResponseEntity<EmptyResponseDto> delete(
      @NotNull(message = SERVER_INVALID)
      @PathVariable("serverId")
      Long serverId,
      @RequestBody @Valid ServerDeleteRequestDto requestDto) {
    serverService.delete(serverId, requestDto);
    return ResponseEntity.ok(null);
  }
}
