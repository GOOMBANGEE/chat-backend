package com.chat.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TokenProvider implements InitializingBean {

  private static final String AUTHORITIES_KEY = "auth";
  private final String secret;
  private final long accessTokenExpireTime;
  private final long refreshTokenExpireTime;
  private Key key;

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer";
  private static final String TOKEN_TYPE = "tokenType";
  private static final String USER_ID = "userId";
  private static final String SUB_SERVER = "subServer";

  private static final String INVALID_TOKEN = "INVALID_TOKEN";

  public TokenProvider(@Value("${jwt.secret}") String secret,
      @Value("${jwt.access-token-expire-time}") long accessTokenExpireTime,
      @Value("${jwt.refresh-token-expire-time}") long refreshTokenExpireTime) {
    this.secret = secret;
    this.accessTokenExpireTime = accessTokenExpireTime;
    this.refreshTokenExpireTime = refreshTokenExpireTime;
  }

  @Override
  public void afterPropertiesSet() {
    byte[] keyBytes = Decoders.BASE64.decode(secret);
    this.key = Keys.hmacShaKeyFor(keyBytes);
  }

  // Authentication 의 권한 정보를 이용해 토큰 생성
  public String createToken(
      Authentication authentication,
      TokenType tokenType,
      Long userId,
      List<Long> serverIdList
  ) {
    // 로그인 시도 유저의 권한들
    String authorities = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.joining(","));
    long now = (new Date()).getTime();

    // 토큰 만료 시간 설정
    Date expires;
    if (tokenType == TokenType.ACCESS_TOKEN) {
      expires = new Date(now + accessTokenExpireTime);
    } else if (tokenType == TokenType.REFRESH_TOKEN) {
      expires = new Date(now + refreshTokenExpireTime);
    } else {
      throw new IllegalArgumentException(INVALID_TOKEN);
    }

    // Create Token
    return Jwts.builder()
        .signWith(key, SignatureAlgorithm.HS512)
        .setSubject(authentication.getName())
        .claim(AUTHORITIES_KEY, authorities)
        .claim(TOKEN_TYPE, tokenType.name())
        // userId
        .claim(USER_ID, userId)
        // 참여중인 서버 목록
        .claim(SUB_SERVER, serverIdList)
        .setExpiration(expires)
        .compact();
  }

  public enum TokenType {
    ACCESS_TOKEN,
    REFRESH_TOKEN
  }

  public String refreshAccessToken(String refreshToken) {
    // 토큰 유효성 검사 및 토큰 종류 확인
    Claims claims = parseClaims(refreshToken);
    String tokenTypeClaim = (String) claims.get(TOKEN_TYPE);

    if (tokenTypeClaim == null || !tokenTypeClaim.equals(TokenType.REFRESH_TOKEN.name())) {
      throw new IllegalArgumentException(INVALID_TOKEN);
    }

    // Access Token 생성
    Authentication authentication = getAuthentication(refreshToken);
    if (authentication == null) {
      throw new IllegalArgumentException(INVALID_TOKEN);
    }
    Long userId = this.getUserIdFromToken(refreshToken);
    List<Long> subServerFromToken = this.getSubServerFromToken(refreshToken);

    return createToken(
        authentication,
        TokenType.ACCESS_TOKEN,
        userId,
        subServerFromToken);
  }

  public String regenerateRefreshToken(
      @NonNull HttpServletRequest request,
      List<Long> subServerList) {
    String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
    String accessToken = bearerToken.substring(BEARER_PREFIX.length()).trim();

    Claims claims = parseClaims(accessToken);
    String tokenTypeClaim = (String) claims.get(TOKEN_TYPE);
    if (tokenTypeClaim == null || !tokenTypeClaim.equals(TokenType.ACCESS_TOKEN.name())) {
      throw new IllegalArgumentException(INVALID_TOKEN);
    }

    Authentication authentication = getAuthentication(accessToken);
    if (authentication == null) {
      throw new IllegalArgumentException(INVALID_TOKEN);
    }
    Long userId = this.getUserIdFromToken(accessToken);
    return createToken(
        authentication,
        TokenType.REFRESH_TOKEN,
        userId,
        subServerList
    );
  }

  public boolean checkTokenSubServerList(HttpServletRequest request,
      List<Long> subServerListFromDB) {
    String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
    String accessToken = bearerToken.substring(BEARER_PREFIX.length()).trim();

    Claims claims = parseClaims(accessToken);
    String tokenTypeClaim = (String) claims.get(TOKEN_TYPE);
    if (tokenTypeClaim == null || !tokenTypeClaim.equals(TokenType.ACCESS_TOKEN.name())) {
      throw new IllegalArgumentException(INVALID_TOKEN);
    }
    Authentication authentication = getAuthentication(accessToken);
    if (authentication == null) {
      throw new IllegalArgumentException(INVALID_TOKEN);
    }

    List<Long> subServerListFromToken = this.getSubServerFromToken(accessToken);
    
    return new HashSet<>(subServerListFromDB).containsAll(subServerListFromToken) &&
        new HashSet<>(subServerListFromToken).containsAll(subServerListFromDB);
  }

  // jwtFilter에서 refreshToken으로 접근못하도록 막는 로직
  public boolean isAccessToken(String accessToken) {
    Claims claims = parseClaims(accessToken);
    String tokenTypeClaim = (String) claims.get(TOKEN_TYPE);

    // refreshToken 요청은 전부 제외
    return claims.get(AUTHORITIES_KEY) != null &&
        !tokenTypeClaim.equals(TokenType.REFRESH_TOKEN.name());
  }

  // accessToken 정보 반환
  public Authentication getAuthentication(String accessToken) {
    // 토큰 복호화
    Claims claims = parseClaims(accessToken);
    log.debug(String.valueOf(claims));
    // refreshToken 요청은 전부 제외
    if (claims.get(AUTHORITIES_KEY) == null) {
      return null;
    }

    // claims 에서 권한 목록 획득
    Collection<? extends GrantedAuthority> authorities = Arrays.stream(
            claims.get(AUTHORITIES_KEY).toString().split(","))
        .map(SimpleGrantedAuthority::new)
        .toList();
    // UserDetails 첨부한 Authentication(인증 정보) 반환
    UserDetails principal = new User(claims.getSubject(), "", authorities);
    return new UsernamePasswordAuthenticationToken(principal, accessToken, authorities);
  }

  // userId 가져오기
  public Long getUserIdFromToken(String token) {
    Claims claims = parseClaims(token);
    Object userIdObj = claims.get(USER_ID);

    if (userIdObj instanceof Integer integer) {
      return integer.longValue();
    } else if (userIdObj instanceof Long longValue) {
      return longValue;
    }
    return null;
  }

  // 참여중인 serverId 리스트 가져오기
  public List<Long> getSubServerFromToken(String token) {
    Claims claims = parseClaims(token);
    Object subServerObj = claims.get(SUB_SERVER);

    if (!(subServerObj instanceof List<?> subServerList)) {
      return Collections.emptyList();
    }

    return subServerList.stream()
        .filter(Number.class::isInstance)
        .map(serverId -> {
          if (serverId instanceof Integer integer) {
            return integer.longValue();
          } else if (serverId instanceof Long longValue) {
            return longValue;
          }
          return null;
        })
        .filter(Objects::nonNull)
        .toList();
  }

  private Claims parseClaims(String token) {
    try {
      return Jwts.parserBuilder().setSigningKey(key).build()
          .parseClaimsJws(token)
          .getBody();
    } catch (ExpiredJwtException e) {
      return e.getClaims();
    }
  }

  // JwtFilter-doFilterInternal에서 OncePerRequestFilter를 통해 요청들어올때 마다 검사
  // header에 jwt가 있을때 jwt값 검증
  // 에러 발생 -> 두 종류 토큰문제, 권한문제
  // 토큰문제인경우 JwtFilter -> JwtAuthenticationEntryPoint
  // 권한문제인경우 JwtFilter -> JwtAccessDeniedHandler
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
      log.info("잘못된 JWT 서명입니다.");
      return false;
    } catch (ExpiredJwtException expiredJwtException) {
      log.info("만료된 JWT 토큰입니다.");
      return false;
    } catch (UnsupportedJwtException e) {
      log.info("미지원 JWT 토큰입니다.");
      return false;
    } catch (IllegalArgumentException e) {
      log.info("잘못된 JWT 토큰입니다.");
      return false;
    }
  }
}
