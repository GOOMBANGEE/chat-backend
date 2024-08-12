package com.chat.service.server;

import com.chat.domain.server.Server;
import com.chat.domain.server.ServerUserRelation;
import com.chat.domain.user.User;
import com.chat.dto.server.ServerCreateRequestDto;
import com.chat.dto.server.ServerCreateResponseDto;
import com.chat.exception.ServerException;
import com.chat.repository.server.ServerRepository;
import com.chat.repository.user.UserRepository;
import com.chat.service.user.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ServerService {

  private final CustomUserDetailsService customUserDetailsService;
  private final ServerRepository serverRepository;
  private final UserRepository userRepository;

  private static final String USER_UNREGISTERED = "SERVER:USER_UNREGISTERED";


  public ServerCreateResponseDto create(ServerCreateRequestDto requestDto) {
    // 등록된 유저인지 확인
    String email = customUserDetailsService.getEmailByUserDetails();
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ServerException(USER_UNREGISTERED));

    // 서버 생성
    String name = requestDto.getName();

    Server server = Server.builder()
        .name(name)
        .description(null)
        .build();

    ServerUserRelation.builder()
        .server(server)
        .user(user)
        .owner(true)
        .build();

    serverRepository.save(server);

    Long id = server.getServerIdForServerCreateResponse();
    return ServerCreateResponseDto.builder()
        .id(id)
        .build();
  }
}
