package com.chat.controller;

import com.chat.dto.AccessTokenDto;
import com.chat.dto.EmptyResponseDto;
import com.chat.dto.JwtTokenDto;
import com.chat.dto.user.EmailCheckRequestDto;
import com.chat.dto.user.FriendAcceptRequestDto;
import com.chat.dto.user.FriendListResponseDto;
import com.chat.dto.user.FriendRejectRequestDto;
import com.chat.dto.user.FriendRequestDto;
import com.chat.dto.user.FriendWaitingListResponseDto;
import com.chat.dto.user.LoginRequestDto;
import com.chat.dto.user.ProfileResponseDto;
import com.chat.dto.user.RecoverConfirmRequestDto;
import com.chat.dto.user.RecoverEmailSendRequestDto;
import com.chat.dto.user.RecoverRequestDto;
import com.chat.dto.user.RecoverTokenCheckResponseDto;
import com.chat.dto.user.RefreshRequestDto;
import com.chat.dto.user.RegisterConfirmRequestDto;
import com.chat.dto.user.RegisterEmailSendRequestDto;
import com.chat.dto.user.RegisterRequestDto;
import com.chat.dto.user.RegisterTokenCheckResponseDto;
import com.chat.dto.user.ResetPasswordRequestDto;
import com.chat.dto.user.ResetUsernameRequestDto;
import com.chat.dto.user.UserDeleteRequestDto;
import com.chat.dto.user.UsernameCheckRequestDto;
import com.chat.exception.UserException;
import com.chat.jwt.TokenProvider;
import com.chat.service.user.UserService;
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
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final TokenProvider tokenProvider;

  private static final String TOKEN_INVALID = "USER:TOKEN_INVALID";

  // 가입시 해당 이메일로 가입된 유저가 있는지 체크
  @PostMapping("/email/check")
  public ResponseEntity<Boolean> emailCheck(
      @RequestBody @Valid EmailCheckRequestDto requestDto) {
    String email = requestDto.getEmail();
    boolean result = userService.validEmailDuplicate(email);
    return ResponseEntity.ok(result);
  }

  // 가입시 해당 사용자명으로 가입된 유저가 있는지 체크
  @PostMapping("/username/check")
  public ResponseEntity<Boolean> usernameCheck(
      @RequestBody @Valid UsernameCheckRequestDto requestDto) {
    String username = requestDto.getUsername();
    boolean result = userService.validUsernameDuplicate(username);
    return ResponseEntity.ok(result);
  }

  // 이메일, 사용자명 중복체크 후 가입로직 진행 후 이메일 발송
  @PostMapping("/register")
  public ResponseEntity<EmptyResponseDto> register(
      @RequestBody @Valid RegisterRequestDto requestDto) {
    userService.register(requestDto);
    return ResponseEntity.ok(null);
  }

  // 이메일 재발송
  @PostMapping("/register/email/send")
  public ResponseEntity<EmptyResponseDto> registerEmailSend(
      @RequestBody @Valid RegisterEmailSendRequestDto requestDto) {
    String email = requestDto.getEmail();
    userService.registerEmailSend(email);
    return ResponseEntity.ok(null);
  }

  // 가입 확인메일에서 진입하는 링크가 유효한 링크인지 검증
  @GetMapping("/{token}/register")
  public ResponseEntity<RegisterTokenCheckResponseDto> registerTokenCheck(
      @NotNull(message = TOKEN_INVALID)
      @PathVariable("token")
      String token) {
    RegisterTokenCheckResponseDto responseDto = userService.registerTokenCheck(token);
    return ResponseEntity.ok(responseDto);
  }

  // 가입 확인메일에서 진입한 링크에서 가입완료
  @PostMapping("/register/confirm")
  public ResponseEntity<EmptyResponseDto> registerConfirm(
      @RequestBody @Valid RegisterConfirmRequestDto requestDto) {
    userService.registerConfirm(requestDto);
    return ResponseEntity.ok(null);
  }

  @PostMapping("/login")
  public ResponseEntity<JwtTokenDto> login(
      @RequestBody @Valid LoginRequestDto requestDto) {
    JwtTokenDto jwtTokenDto = userService.login(requestDto);
    return ResponseEntity.ok(jwtTokenDto);
  }

  // accessToken refresh
  @PostMapping("/refresh")
  public ResponseEntity<AccessTokenDto> refresh(
      @RequestBody @Valid RefreshRequestDto requestDto) {
    String refreshToken = requestDto.getRefreshToken();
    // refresh token 유효성 검사
    if (!tokenProvider.validateToken(refreshToken)) {
      throw new UserException(TOKEN_INVALID);
    }
    // refresh token으로 access token 재발급
    AccessTokenDto accessTokenDto = tokenProvider.refreshAccessToken(refreshToken);
    return ResponseEntity.ok(accessTokenDto);
  }

  // 사용자정보 fetch
  @GetMapping("/profile")
  public ResponseEntity<ProfileResponseDto> profile() {
    ProfileResponseDto responseDto = userService.profile();
    return ResponseEntity.ok(responseDto);
  }

  // 비밀번호 분실히 비밀번호 찾기
  @PostMapping("/recover")
  public ResponseEntity<EmptyResponseDto> recover(
      @RequestBody @Valid RecoverRequestDto requestDto) {
    userService.recover(requestDto);
    return ResponseEntity.ok(null);
  }

  // 비밀번호 분실시 발송했던 이메일을 재발송
  @PostMapping("/recover/email/send")
  public ResponseEntity<EmptyResponseDto> recoverEmailSend(
      @RequestBody @Valid RecoverEmailSendRequestDto requestDto) {
    String email = requestDto.getEmail();
    userService.recoverEmailSend(email);
    return ResponseEntity.ok(null);
  }

  // 비밀번호 복구 메일에서 진입하는 링크가 유효한 링크인지 검증
  @GetMapping("/{token}/recover")
  public ResponseEntity<RecoverTokenCheckResponseDto> recoverTokenCheck(
      @NotNull(message = TOKEN_INVALID)
      @PathVariable("token")
      String token) {
    RecoverTokenCheckResponseDto responseDto = userService.recoverTokenCheck(token);
    return ResponseEntity.ok(responseDto);
  }

  // 비밀번호 복구 재설정
  @PostMapping("/recover/confirm")
  public ResponseEntity<EmptyResponseDto> recoverConfirm(
      @RequestBody @Valid RecoverConfirmRequestDto requestDto) {
    userService.recoverConfirm(requestDto);
    return ResponseEntity.ok(null);
  }

  // 비밀번호 재설정
  @PostMapping("/reset/password")
  public ResponseEntity<EmptyResponseDto> resetPassword(
      @RequestBody @Valid ResetPasswordRequestDto requestDto) {
    userService.resetPassword(requestDto);
    return ResponseEntity.ok(null);
  }

  // 사용자명 재설정
  @PostMapping("/reset/username")
  public ResponseEntity<EmptyResponseDto> resetUsername(
      @RequestBody @Valid ResetUsernameRequestDto requestDto) {
    userService.resetUsername(requestDto);
    return ResponseEntity.ok(null);
  }

  @PostMapping("/delete")
  public ResponseEntity<EmptyResponseDto> userDelete(
      @RequestBody @Valid UserDeleteRequestDto requestDto) {
    userService.userDelete(requestDto);
    return ResponseEntity.ok(null);
  }

  // 친구 신청
  @PostMapping("/friend")
  public ResponseEntity<EmptyResponseDto> friend(
      @RequestBody @Valid FriendRequestDto requestDto) {
    userService.friend(requestDto);
    return ResponseEntity.ok(null);
  }

  // 친구 요청 대기중인 정보 가져오기
  @GetMapping("/friend/waiting/list")
  public ResponseEntity<FriendWaitingListResponseDto> friendWaitingList() {
    FriendWaitingListResponseDto responseDto = userService.friendWaitingList();
    return ResponseEntity.ok(responseDto);
  }

  // 친구 리스트 정보 가져오기
  @GetMapping("/friend/list")
  public ResponseEntity<FriendListResponseDto> friendList() {
    FriendListResponseDto responseDto = userService.friendList();
    return ResponseEntity.ok(responseDto);
  }

  // 친구신청 수락
  @PostMapping("/friend/accept")
  public ResponseEntity<EmptyResponseDto> friendAccept(
      @RequestBody @Valid FriendAcceptRequestDto requestDto) {
    userService.friendAccept(requestDto);
    return ResponseEntity.ok(null);
  }

  // 친구신청 거절
  @PostMapping("/friend/reject")
  public ResponseEntity<EmptyResponseDto> friendReject(
      @RequestBody @Valid FriendRejectRequestDto requestDto) {
    userService.friendReject(requestDto);
    return ResponseEntity.ok(null);
  }
}
