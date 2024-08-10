package com.chat.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer";
  private final TokenProvider tokenProvider;

  // OncePerRequestFilter로 모든 요청이 들어올때마다 실행
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String jwt = resolveToken(request);
    String requestURI = request.getRequestURI();

    // header에 jwt가 있을때 jwt값 검증
    if (StringUtils.hasText(jwt) &&
        tokenProvider.validateToken(jwt) &&
        tokenProvider.isAccessToken(jwt)) {
      // refreshToken 요청은 전부 제외
      Authentication authentication = tokenProvider.getAuthentication(jwt);

      SecurityContextHolder.getContext().setAuthentication(authentication);
      log.debug("Security Context에 '{}' 인증 정보를 저장했습니다, uri:{} token: {}",
          authentication.getName(), requestURI, jwt);
    } else {
      request.setAttribute("exception", "invalid_token");
    }
    filterChain.doFilter(request, response);
  }

  // 요청 헤더 또는 쿠키에서 jwt 추출
  private String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
      return bearerToken.substring(BEARER_PREFIX.length()).trim();
    }
    return null;
  }
}
