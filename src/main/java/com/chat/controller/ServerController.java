package com.chat.controller;

import com.chat.dto.EmptyResponseDto;
import com.chat.dto.server.ServerCreateRequestDto;
import com.chat.dto.server.ServerCreateResponseDto;
import com.chat.dto.server.ServerDeleteRequestDto;
import com.chat.dto.server.ServerInfoDto;
import com.chat.dto.server.ServerInviteInfoResponseDto;
import com.chat.dto.server.ServerInviteResponseDto;
import com.chat.dto.server.ServerJoinResponseDto;
import com.chat.dto.server.ServerListResponseDto;
import com.chat.dto.server.ServerSettingRequestDto;
import com.chat.dto.server.ServerUserListResponseDto;
import com.chat.service.server.ServerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
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
  private static final String REFRESH_TOKEN = "Refresh-Token";

  // 서버 생성
  @PostMapping("/create")
  public ResponseEntity<ServerCreateResponseDto> create(
      HttpServletRequest request,
      @RequestBody @Valid ServerCreateRequestDto requestDto) {
    Pair<ServerCreateResponseDto, String> responsePair = serverService.create(request, requestDto);

    ServerCreateResponseDto responseDto = responsePair.getLeft();
    String responseRefreshToken = responsePair.getRight();
    return ResponseEntity.ok()
        .header(REFRESH_TOKEN, responseRefreshToken)
        .body(responseDto);
  }

  // 참여중인 서버 목록
  @GetMapping("/list")
  public ResponseEntity<ServerListResponseDto> list(HttpServletRequest request) {
    Pair<List<ServerInfoDto>, String> responsePair = serverService.list(request);

    ServerListResponseDto serverListResponseDto = ServerListResponseDto.builder()
        .serverList(responsePair.getLeft())
        .build();
    String responseRefreshToken = responsePair.getRight();
    return ResponseEntity.ok()
        .header(REFRESH_TOKEN, responseRefreshToken)
        .body(serverListResponseDto);
  }

  // 서버 설정변경
  @PostMapping("/{serverId}/setting")
  public ResponseEntity<EmptyResponseDto> setting(
      @NotNull(message = SERVER_INVALID)
      @PathVariable("serverId")
      Long serverId,
      @RequestBody @Valid ServerSettingRequestDto requestDto) throws JsonProcessingException {
    serverService.setting(serverId, requestDto);
    return ResponseEntity.ok(null);
  }


  // 서버 입장
  @PostMapping("/{code}/join")
  public ResponseEntity<ServerJoinResponseDto> join(
      HttpServletRequest request,
      @NotNull(message = SERVER_INVALID)
      @PathVariable("code")
      String code) {
    Pair<ServerJoinResponseDto, String> responsePair = serverService.join(request, code);
    ServerJoinResponseDto responseDto = responsePair.getLeft();
    String responseRefreshToken = responsePair.getRight();
    return ResponseEntity.ok()
        .header(REFRESH_TOKEN, responseRefreshToken)
        .body(responseDto);
  }

  // 서버 초대코드 조회
  @GetMapping("/{code}/invite")
  public ResponseEntity<ServerInviteInfoResponseDto> inviteInfo(
      @NotNull(message = SERVER_INVALID)
      @PathVariable("code")
      String code) {
    ServerInviteInfoResponseDto responseDto = serverService.inviteInfo(code);
    return ResponseEntity.ok(responseDto);
  }

  // 서버 초대코드 생성
  @PostMapping("/{serverId}/invite")
  public ResponseEntity<ServerInviteResponseDto> invite(
      @NotNull(message = SERVER_INVALID)
      @PathVariable("serverId")
      Long serverId) {
    ServerInviteResponseDto responseDto = serverService.invite(serverId);
    return ResponseEntity.ok(responseDto);
  }

  // 서버 나가기
  @PostMapping("/{serverId}/leave")
  public ResponseEntity<EmptyResponseDto> leave(
      HttpServletRequest request,
      @NotNull(message = SERVER_INVALID)
      @PathVariable("serverId")
      Long serverId) {
    String responseRefreshToken = serverService.leave(request, serverId);
    return ResponseEntity.ok()
        .header(REFRESH_TOKEN, responseRefreshToken)
        .build();
  }


  // 서버 삭제
  @PostMapping("/{serverId}/delete")
  public ResponseEntity<EmptyResponseDto> delete(
      HttpServletRequest request,
      @NotNull(message = SERVER_INVALID)
      @PathVariable("serverId")
      Long serverId,
      @RequestBody @Valid ServerDeleteRequestDto requestDto) {
    String responseRefreshToken = serverService.delete(request, serverId, requestDto);
    return ResponseEntity.ok()
        .header(REFRESH_TOKEN, responseRefreshToken)
        .build();
  }

  // 서버 유저 목록
  @GetMapping("/{serverId}/list/user")
  public ResponseEntity<ServerUserListResponseDto> userList(
      @NotNull(message = SERVER_INVALID)
      @PathVariable("serverId")
      Long serverId) {
    ServerUserListResponseDto responseDto = serverService.userList(serverId);
    return ResponseEntity.ok(responseDto);
  }
}
