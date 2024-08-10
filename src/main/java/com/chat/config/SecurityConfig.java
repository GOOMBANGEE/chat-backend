package com.chat.config;

import com.chat.jwt.JwtAccessDeniedHandler;
import com.chat.jwt.JwtAuthenticationEntryPoint;
import com.chat.jwt.JwtFilter;
import com.chat.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsUtils;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final TokenProvider tokenProvider;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

  // 필터를 거치지 않고 html, js, css 파일 접근 허용
  @Bean
  public WebSecurityCustomizer configure() {
    return web -> web.ignoring().requestMatchers("/static/**");
  }

  // http 웹 기반 보안 구성
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        // REST >> csrf 사용하지않음 // csrf >> basic auth 에서만 사용
        .csrf(CsrfConfigurer::disable)
        // 세션을 미사용 -> STATELESS 설정
        .sessionManagement(
            configurer -> configurer
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // 인증, 접근 예외 핸들링 추가
        .exceptionHandling(
            authenticationManager -> authenticationManager
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler))
        // 페이지 접근 권한 설정
        .authorizeHttpRequests(
            authorize -> authorize
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                .requestMatchers("/**").permitAll())
        // JwtFilter 보안 설정 적용
        .addFilterBefore(new JwtFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class);
    return httpSecurity.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
