package com.chat.service.user;

import com.chat.domain.user.Role;
import com.chat.domain.user.User;
import com.chat.domain.user.UserRole;
import com.chat.domain.user.UserTemp;
import com.chat.domain.user.UserTempReset;
import com.chat.dto.JwtTokenDto;
import com.chat.dto.user.LoginRequestDto;
import com.chat.dto.user.ProfileResponseDto;
import com.chat.dto.user.RecoverConfirmRequestDto;
import com.chat.dto.user.RecoverRequestDto;
import com.chat.dto.user.RecoverTokenCheckResponseDto;
import com.chat.dto.user.RegisterConfirmRequestDto;
import com.chat.dto.user.RegisterRequestDto;
import com.chat.dto.user.RegisterTokenCheckResponseDto;
import com.chat.dto.user.ResetPasswordRequestDto;
import com.chat.dto.user.ResetUsernameRequestDto;
import com.chat.exception.UserException;
import com.chat.jwt.TokenProvider;
import com.chat.jwt.TokenProvider.TokenType;
import com.chat.repository.user.RoleRepository;
import com.chat.repository.user.UserRepository;
import com.chat.repository.user.UserRoleRepository;
import com.chat.repository.user.UserTempRepository;
import com.chat.repository.user.UserTempResetRepository;
import com.chat.service.MailService;
import com.chat.util.UUIDGenerator;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final UserRoleRepository userRoleRepository;
  private final UserTempRepository userTempRepository;
  private final UserTempResetRepository userTempResetRepository;
  private final AuthenticationManagerBuilder authenticationManagerBuilder;
  private final TokenProvider tokenProvider;
  private final PasswordEncoder passwordEncoder;
  private final MailService mailService;
  private final RoleRepository roleRepository;
  private final UUIDGenerator uuidGenerator;

  private static final String EMAIL_EXIST = "USER:EMAIL_EXIST";
  private static final String USERNAME_EXIST = "USER:USERNAME_EXIST";
  private static final String USER_UNREGISTERED = "USER:USER_UNREGISTERED";
  private static final String PASSWORD_MISMATCH = "USER:PASSWORD_MISMATCH";
  private static final String TOKEN_INVALID = "USER:TOKEN_INVALID";
  private static final String EMAIL_OR_PASSWORD_ERROR = "USER:EMAIL_OR_PASSWORD_ERROR";
  private static final String EMAIL_ACTIVATE_REQUIRE = "USER:EMAIL_ACTIVATE_REQUIRE";

  @Value("${server.front-url}")
  private String frontUrl;

  // 인증 유저정보 가져오기 username == email
  private String getEmailByUserDetails() {
    UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
    return principal.getUsername();
  }

  // 가입시 해당 이메일로 가입된 유저가 있는지 체크
  public Boolean validEmailDuplicate(String email) {
    Optional<User> user = userRepository.findByEmail(email);

    // 이미존재 -> exception
    if (user.isPresent()) {
      throw new UserException(EMAIL_EXIST);
    } else {
      // 아닌경우 true
      return true;
    }
  }

  // 가입시 해당 사용자명으로 가입된 유저가 있는지 체크
  public Boolean validUsernameDuplicate(String username) {
    Optional<User> user = userRepository.findByUsername(username);

    // 이미존재 -> exception
    if (user.isPresent()) {
      throw new UserException(USERNAME_EXIST);
    } else {
      // 아닌경우 true
      return true;
    }
  }

  // 이메일, 사용자명 중복검사 후 등록이메일 발송
  @Transactional
  public void register(RegisterRequestDto requestDto) {
    String email = requestDto.getEmail();
    String username = requestDto.getUsername();
    String password = requestDto.getPassword();
    String confirmPassword = requestDto.getConfirmPassword();

    // 비밀번호 불일치 검사
    if (!password.equals(confirmPassword)) {
      throw new UserException(PASSWORD_MISMATCH);
    }

    // 이메일, 사용자명 중복검사 -> 이메일발송
    validEmailDuplicate(email);
    validUsernameDuplicate(username);

    LocalDateTime registerDate = LocalDateTime.now();
    String token = uuidGenerator.generateUUID();

    Role role = Role.builder()
        .roleName("ROLE_NONE")
        .build();

    User user = User.builder()
        .email(email)
        .username(username)
        .password(passwordEncoder.encode(password))
        .registerDate(registerDate)
        .activated(false)
        .build();

    // jwt role
    UserRole userRole = UserRole.builder()
        .user(user)
        .role(role)
        .build();

    UserTemp userTemp = UserTemp.builder()
        .token(token)
        .user(user)
        .build();

    String verificationLink = frontUrl + "/user/register/" + token;
    mailService.sendEmail(email, "이메일 인증 메일 입니다", verificationLink);

    roleRepository.save(role);
    userRepository.save(user);
    userRoleRepository.save(userRole);
    userTempRepository.save(userTemp);
  }

  // 이메일 발송
  public void registerEmailSend(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));
    UserTemp userTemp = userTempRepository.findByUser(user)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    // 이메일 재발송을 위한 토큰
    String token = userTemp.tokenForResend();
    String verificationLink = frontUrl + "/user/register/" + token;

    mailService.sendEmail(email, "이메일 인증 메일 입니다", verificationLink);
  }

  // 가입 확인메일에서 진입하는 링크가 유효한 링크인지 검증
  public RegisterTokenCheckResponseDto registerTokenCheck(String token) {
    UserTemp userTemp = userTempRepository.findByToken(token)
        .orElseThrow(() -> new UserException(TOKEN_INVALID));

    // token을 통해 userTemp.user -> user.email 가져옴
    String email = userTemp.fetchEmailByToken();

    return RegisterTokenCheckResponseDto.builder()
        .email(email)
        .build();
  }

  // 이메일 인증으로 유저 활성화
  @Transactional
  public void registerConfirm(RegisterConfirmRequestDto registerConfirmRequestDto) {
    String token = registerConfirmRequestDto.getToken();
    String email = registerConfirmRequestDto.getEmail();

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));
    UserTemp userTemp = userTempRepository.findByToken(token)
        .orElseThrow(() -> new UserException(TOKEN_INVALID));

    user.activate();

    userRepository.save(user);
    userTempRepository.delete(userTemp);
  }

  public JwtTokenDto login(LoginRequestDto loginRequestDto) {
    String email = loginRequestDto.getEmail();
    String password = loginRequestDto.getPassword();
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    if (!user.checkActivated()) {
      throw new UserException(EMAIL_ACTIVATE_REQUIRE);
    }

    try {
      // 1. Login Email, Password 로 AuthenticationToken 생성
      UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
          email, password);

      // 2. Email, Password 일치 검증이 일어남
      //    authenticationManagerBuilder.getObject().authenticate() 에서 CustomUserDetailsService 의 loadUserByUsername() 실행됨
      Authentication authentication = authenticationManagerBuilder.getObject()
          .authenticate(authenticationToken);
      SecurityContextHolder.getContext().setAuthentication(authentication);

      // 3. 인증 정보로 JWT 토큰 생성
      // 4. 토큰 발급
      String accessToken = tokenProvider.createToken(authentication, TokenType.ACCESS_TOKEN);
      String refreshToken = tokenProvider.createToken(authentication, TokenType.REFRESH_TOKEN);

      return JwtTokenDto.builder()
          .accessToken(accessToken)
          .refreshToken(refreshToken)
          .build();
    } catch (Exception e) {
      throw new UserException(EMAIL_OR_PASSWORD_ERROR);
    }
  }

  // 사용자정보 fetch
  public ProfileResponseDto profile() {
    String email = getEmailByUserDetails();
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));
    return user.buildProfileResponseDto();
  }

  // 비밀번호 분실히 비밀번호 찾기
  @Transactional
  public void recover(RecoverRequestDto recoverRequestDto) {
    String email = recoverRequestDto.getEmail();

    // 해당 이메일로 가입된 유저가 없는경우
    User user = userRepository.findByEmail(email).orElse(null);
    if (user == null) {
      mailService.sendEmail(email, "비밀번호 초기화 링크입니다", "해당 이메일로 가입된 정보가 없습니다");
      return;
    }

    // 비밀번호 찾기를 시도한 경우가 있는가?
    Optional<UserTempReset> userTempReset = userTempResetRepository.findByUser(user);
    String token = null;

    // 비밀번호 찾기의 token이 없는경우 생성 후 이메일 발송
    if (userTempReset.isEmpty()) {
      token = uuidGenerator.generateUUID();
      UserTempReset newUserTempReset = UserTempReset.builder()
          .token(token)
          .user(user)
          .build();
      userTempResetRepository.save(newUserTempReset);
    }

    // 비밀번호 찾기의 token이 있는 경우 기존 token 가져옴
    if (userTempReset.isPresent()) {
      token = userTempReset.get().tokenForResend();
    }

    // 이메일 발송
    String recoverUrl = frontUrl + "/user/recover/" + token;
    mailService.sendEmail(email, "비밀번호 초기화 링크입니다", recoverUrl);
  }

  // 비밀번호 분실시 발송했던 이메일을 재발송
  public void recoverEmailSend(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));
    UserTempReset userTempReset = userTempResetRepository.findByUser(user)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));
    String token = userTempReset.tokenForResend();
    String verificationLink = frontUrl + "/user/recover/" + token;
    mailService.sendEmail(email, "비밀번호 초기화 링크입니다", verificationLink);
  }

  // 비밀번호 복구 메일에서 진입하는 링크가 유효한 링크인지 검증
  public RecoverTokenCheckResponseDto recoverTokenCheck(String token) {
    Optional<UserTempReset> userTempReset = userTempResetRepository.findByToken(token);
    if (userTempReset.isEmpty()) {
      throw new UserException(USER_UNREGISTERED);
    }

    String email = userTempReset.get().fetchEmailByToken();
    return RecoverTokenCheckResponseDto.builder()
        .email(email)
        .build();
  }

  // 비밀번호 복구 재설정
  @Transactional
  public void recoverConfirm(RecoverConfirmRequestDto recoverConfirmRequestDto) {
    String token = recoverConfirmRequestDto.getToken();
    String email = recoverConfirmRequestDto.getEmail();
    String password = passwordEncoder.encode(recoverConfirmRequestDto.getPassword());

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));
    UserTempReset userTempReset = userTempResetRepository.findByToken(token)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    // 비밀번호 복구 재설정 userTemp.user -> user.password
    userTempReset.recoverPassword(password);

    userRepository.save(user);
    userTempResetRepository.delete(userTempReset);
  }

  // 비밀번호 재설정
  @Transactional
  public void resetPassword(ResetPasswordRequestDto resetPasswordRequestDto) {
    String email = getEmailByUserDetails();
    String prevPassword = resetPasswordRequestDto.getPrevPassword();
    String newPassword = passwordEncoder.encode(resetPasswordRequestDto.getNewPassword());

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));
    // 비밀번호 재설정시 이전 비밀번호 확인
    if (user.checkPassword(prevPassword, passwordEncoder)) {
      user.resetPassword(newPassword);
      userRepository.save(user);
      return;
    }
    throw new UserException(PASSWORD_MISMATCH);
  }

  // 사용자명 재설정
  @Transactional
  public void resetUsername(ResetUsernameRequestDto requestDto) {
    String email = getEmailByUserDetails();
    String username = requestDto.getUsername();
    // 유저검색
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    // 사용자명 중복 검사
    validUsernameDuplicate(username);

    // 사용자명 재설정
    user.resetUsername(username);
    userRepository.save(user);
  }

  // 유저 삭제
  @Transactional
  public void userDelete() {
    String email = getEmailByUserDetails();
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));
    user.userDelete();
    userRepository.delete(user);
  }
}
