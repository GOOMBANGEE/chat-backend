package com.chat.jwt;

import com.chat.dto.ErrorResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sentry.Sentry;
import io.sentry.protocol.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authenticationException) throws IOException {
    String exception = (String) request.getAttribute("exception");

    if (Objects.equals(exception, "invalid_token")) {
      log.debug("JwtAuthenticationEntryPoint 발생");
      response.setStatus(HttpStatus.BAD_REQUEST.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setCharacterEncoding("UTF-8");
      objectMapper.writeValue(response.getWriter(),
          ErrorResponseDto.build("TOKEN:INVALID_TOKEN", "유효하지않은 토큰입니다"));
      configureSentryScope("TOKEN:INVALID_TOKEN", request);
      return;
    }
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(response.getWriter(),
        ErrorResponseDto.build("TOKEN:UNAUTHORIZED", "허가되지 않은 유저입니다"));
    configureSentryScope("TOKEN:UNAUTHORIZED", request);
  }

  private void configureSentryScope(String id, HttpServletRequest request) {
    Sentry.withScope(scope -> {
      // IP 항상 기본으로 수집
      User user = new User();
      user.setIpAddress(request.getRemoteAddr());
      Sentry.setUser(user);
      Sentry.captureMessage(id);
    });
  }
}
