package com.chat.controller;

import com.chat.dto.server.ServerCreateRequestDto;
import com.chat.dto.server.ServerCreateResponseDto;
import com.chat.service.server.ServerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/server")
@RequiredArgsConstructor
public class ServerController {

  private final ServerService serverService;

  // 서버 생성
  @PostMapping("/")
  public ResponseEntity<ServerCreateResponseDto> create(
      @RequestBody @Valid ServerCreateRequestDto requestDto) {
    ServerCreateResponseDto responseDto = serverService.create(requestDto);
    return ResponseEntity.ok(responseDto);
  }

  // 서버 설정변경

  // 서버 서버 입장

  // 서버 초대

  // 서버 삭제
}
