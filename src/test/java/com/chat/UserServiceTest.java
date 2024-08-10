package com.chat;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.chat.repository.user.RoleRepository;
import com.chat.repository.user.UserRepository;
import com.chat.repository.user.UserRoleRepository;
import com.chat.repository.user.UserTempRepository;
import com.chat.repository.user.UserTempResetRepository;
import com.chat.service.MailService;
import com.chat.service.user.UserService;
import com.chat.util.UUIDGenerator;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @InjectMocks
  private UserService userService;

  @Mock
  private MailService mailService;

  @Mock
  private UserRepository userRepository;
  @Mock
  private UserRoleRepository userRoleRepository;
  @Mock
  private UserTempRepository userTempRepository;
  @Mock
  private UserTempResetRepository userTempResetRepository;
  @Mock
  private RoleRepository roleRepository;

  @Mock
  private AuthenticationManagerBuilder authenticationManagerBuilder;
  @Mock
  private AuthenticationManager authenticationManager;
  @Mock
  private TokenProvider tokenProvider;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private UUIDGenerator uuidGenerator;

  @Mock
  UserDetails userDetails;
  @Mock
  SecurityContext securityContext;
  @Mock
  Authentication authentication;

  private static final String EMAIL_EXIST = "USER:EMAIL_EXIST";
  private static final String USERNAME_EXIST = "USER:USERNAME_EXIST";
  private static final String USER_UNREGISTERED = "USER:USER_UNREGISTERED";
  private static final String PASSWORD_MISMATCH = "USER:PASSWORD_MISMATCH";
  private static final String TOKEN_INVALID = "USER:TOKEN_INVALID";
  private static final String EMAIL_OR_PASSWORD_ERROR = "USER:EMAIL_OR_PASSWORD_ERROR";
  private static final String EMAIL_ACTIVATE_REQUIRE = "USER:EMAIL_ACTIVATE_REQUIRE";

  private User testUser;
  private User testUserNotActivated;
  private UserTemp testUserTemp;
  private UserTempReset testUserTempReset;

  private String testEmail;
  private String testUsername;
  private String testPassword;
  private String testDiffPassword;
  private String testToken;
  private String frontUrl;

  @BeforeEach
  void setUp() {
    testEmail = "test@test.com";
    testUsername = "test";
    testPassword = "1q2w3e4r!";
    testDiffPassword = "diffPassword";
    testToken = "test";
    frontUrl = "null";

    when(passwordEncoder.encode(testPassword)).thenReturn(testPassword);

    testUser = User.builder()
        .email(testEmail)
        .username(testUsername)
        .password(passwordEncoder.encode(testPassword))
        .registerDate(LocalDateTime.MIN)
        .activated(true)
        .build();

    testUserNotActivated = User.builder()
        .email(testEmail)
        .username(testUsername)
        .password(passwordEncoder.encode(testPassword))
        .registerDate(LocalDateTime.MIN)
        .activated(false)
        .build();

    testUserTemp = UserTemp.builder()
        .user(testUserNotActivated)
        .token(testToken)
        .build();

    testUserTempReset = UserTempReset.builder()
        .user(testUser)
        .token(testToken)
        .build();
  }

  void testUserDetails() {
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(userDetails.getUsername()).thenReturn(testEmail);
  }

  @Test
  void testValidEmailDuplicate_SUCCESS() {
    // given
    String email = testEmail;
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    // when
    Boolean result = userService.validEmailDuplicate(testEmail);

    // then
    assertTrue(result);
  }

  // 가입시 해당 이메일로 가입된 유저가 있는지 체크
  @Test
  void testValidEmailDuplicate_EMAIL_EXIST() {
    // given
    String email = testEmail;
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

    // when
    UserException exception = assertThrows(UserException.class,
        () -> userService.validEmailDuplicate(testEmail));

    // then
    assertEquals(EMAIL_EXIST, exception.getId());
  }

  @Test
  void testValidUsernameDuplicate_SUCCESS() {
    // given
    String username = testUsername;
    when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

    // when
    Boolean result = userService.validUsernameDuplicate(testUsername);

    // then
    assertTrue(result);
  }

  @Test
  void testValidUsernameDuplicate_USERNAME_EXIST() {
    // given
    String username = testUsername;
    when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

    // when
    UserException exception = assertThrows(UserException.class,
        () -> userService.validUsernameDuplicate(testUsername));

    // then
    assertEquals(USERNAME_EXIST, exception.getId());
  }

  @Test
  void testRegister_SUCCESS() {
    // given
    RegisterRequestDto requestDto = RegisterRequestDto.builder()
        .email(testEmail)
        .username(testUsername)
        .password(testPassword)
        .confirmPassword(testPassword)
        .build();
    String email = requestDto.getEmail();
    String username = requestDto.getUsername();

    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    when(uuidGenerator.generateUUID()).thenReturn(testToken);
    String expectedVerificationLink = frontUrl + "/user/register/" + testToken;

    // when
    userService.register(requestDto);

    // then
    Optional<User> registeredUser = userRepository.findByEmail(email);
    assertAll(
        () -> verify(roleRepository, times(1)).save(any(Role.class)),
        () -> verify(userRepository, times(1)).save(any(User.class)),
        () -> verify(userRoleRepository, times(1)).save(any(UserRole.class)),
        () -> verify(userTempRepository, times(1)).save(any(UserTemp.class))
    );
    verify(mailService).sendEmail(email, "이메일 인증 메일 입니다", expectedVerificationLink);
    registeredUser.ifPresent(user -> assertTrue(user.checkUsername(username)));
  }

  @Test
  void testRegister_PASSWORD_MISMATCH() {
    // given
    RegisterRequestDto requestDto = RegisterRequestDto.builder()
        .email(testEmail)
        .username(testUsername)
        .password(testPassword)
        .confirmPassword(testDiffPassword)
        .build();

    // when
    UserException exception = assertThrows(UserException.class,
        () -> userService.register(requestDto));

    // then
    assertEquals(PASSWORD_MISMATCH, exception.getId());
  }

  @Test
  void testRegisterEmailSend_SUCCESS() {
    // given
    String email = testEmail;
    String expectedVerificationLink = frontUrl + "/user/register/" + testToken;

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
    when(userTempRepository.findByUser(testUser)).thenReturn(Optional.of(testUserTemp));

    // when
    userService.registerEmailSend(email);

    // then
    verify(mailService).sendEmail(email, "이메일 인증 메일 입니다", expectedVerificationLink);
  }

  @Test
  void testRegisterEmailSend_USER_UNREGISTERED() {
    // given
    String email = testEmail;
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    // when
    UserException exception = assertThrows(UserException.class,
        () -> userService.registerEmailSend(email));

    // then
    assertEquals(USER_UNREGISTERED, exception.getId());
  }

  @Test
  void testRegisterEmailSend_UserTempNotExist() {
    // given
    String email = testEmail;
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
    when(userTempRepository.findByUser(testUser)).thenReturn(Optional.empty());

    // when
    UserException exception = assertThrows(UserException.class,
        () -> userService.registerEmailSend(email));

    // then
    assertEquals(USER_UNREGISTERED, exception.getId());
  }

  @Test
  void testRegisterTokenCheck_SUCCESS() {
    // given
    String token = testToken;
    when(userTempRepository.findByToken(token)).thenReturn(Optional.of(testUserTemp));

    // when
    RegisterTokenCheckResponseDto responseDto = userService.registerTokenCheck(token);

    // then
    assertEquals(testEmail, responseDto.getEmail());
  }

  @Test
  void testRegisterTokenCheck_USER_UNREGISTERED() {
    // given
    String token = testToken;
    when(userTempRepository.findByToken(token)).thenReturn(Optional.empty());

    // when
    UserException exception = assertThrows(UserException.class,
        () -> userService.registerTokenCheck(token));

    // then
    assertEquals(TOKEN_INVALID, exception.getId());
  }

  @Test
  void testRegisterConfirm_SUCCESS() {
    // given
    RegisterConfirmRequestDto requestDto = RegisterConfirmRequestDto.builder()
        .token(testToken)
        .email(testEmail)
        .build();
    String token = requestDto.getToken();
    String email = requestDto.getEmail();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUserNotActivated));
    when(userTempRepository.findByToken(token)).thenReturn(Optional.of(testUserTemp));

    // when
    userService.registerConfirm(requestDto);

    // then
    verify(userRepository, times(1)).save(any(User.class));
    verify(userTempRepository, times(1)).delete(any(UserTemp.class));
    assertTrue(testUserNotActivated.checkActivated());
  }

  @Test
  void testRegisterConfirm_USER_UNREGISTERED() {
    // given
    RegisterConfirmRequestDto requestDto = RegisterConfirmRequestDto.builder()
        .token(testToken)
        .email(testEmail)
        .build();
    String email = requestDto.getEmail();

    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    // when
    UserException exception = assertThrows(UserException.class,
        () -> userService.registerConfirm(requestDto));

    // then
    assertEquals(USER_UNREGISTERED, exception.getId());
  }

  @Test
  void testRegisterConfirm_TOKEN_INVALID() {
    // given
    RegisterConfirmRequestDto requestDto = RegisterConfirmRequestDto.builder()
        .token(testToken)
        .email(testEmail)
        .build();
    String token = requestDto.getToken();
    String email = requestDto.getEmail();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
    when(userTempRepository.findByToken(token)).thenReturn(Optional.empty());

    // when
    UserException exception = assertThrows(UserException.class,
        () -> userService.registerConfirm(requestDto));

    // then
    assertEquals(TOKEN_INVALID, exception.getId());
  }

  @Test
  void testLogin_SUCCESS() {
    // given
    LoginRequestDto requestDto = LoginRequestDto.builder()
        .email(testEmail)
        .password(testPassword)
        .build();
    String email = requestDto.getEmail();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

    when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);
    when(tokenProvider.createToken(any(Authentication.class), any(TokenProvider.TokenType.class)))
        .thenReturn(testToken);

    //when
    JwtTokenDto tokenDto = userService.login(requestDto);

    // then
    assertNotNull(tokenDto);
    assertEquals(testToken, tokenDto.getAccessToken());
    assertEquals(testToken, tokenDto.getRefreshToken());
  }

  @Test
  void testLogin_USER_UNREGISTERED() {
    // given
    LoginRequestDto requestDto = LoginRequestDto.builder()
        .email(testEmail)
        .password(testDiffPassword)
        .build();
    String email = requestDto.getEmail();

    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    // when
    UserException exception = assertThrows(UserException.class,
        () -> userService.login(requestDto));

    // then
    assertEquals(USER_UNREGISTERED, exception.getId());
  }

  @Test
  void testLogin_EMAIL_ACTIVATE_REQUIRE() {
    // given
    LoginRequestDto requestDto = LoginRequestDto.builder()
        .email(testEmail)
        .password(testPassword)
        .build();
    String email = requestDto.getEmail();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUserNotActivated));

    // when
    UserException exception = assertThrows(UserException.class,
        () -> userService.login(requestDto));

    // then
    assertEquals(EMAIL_ACTIVATE_REQUIRE, exception.getId());
  }

  @Test
  void testLogin_EMAIL_OR_PASSWORD_ERROR() {
    // given
    LoginRequestDto requestDto = LoginRequestDto.builder()
        .email(testEmail)
        .password(testDiffPassword)
        .build();
    String email = requestDto.getEmail();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

    // when
    UserException exception = assertThrows(UserException.class,
        () -> userService.login(requestDto));

    // then
    assertEquals(EMAIL_OR_PASSWORD_ERROR, exception.getId());
  }

  @Test
  void testProfile_SUCCESS() {
    // given
    testUserDetails();
    String email = testEmail;
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

    // when
    ProfileResponseDto profile = userService.profile();

    // then
    assertNotNull(profile);
    assertEquals(testEmail, profile.getEmail());
  }

  @Test
  void testProfile_USER_UNREGISTERED() {
    // given
    testUserDetails();
    String email = testEmail;
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    // when
    UserException exception = assertThrows(UserException.class,
        () -> userService.profile());

    // then
    assertEquals(USER_UNREGISTERED, exception.getId());
  }

  @Test
  void recover_SUCCESS_UserTempResetExist() {
    // given
    RecoverRequestDto requestDto = RecoverRequestDto.builder()
        .email(testEmail)
        .build();
    String email = requestDto.getEmail();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
    when(userTempResetRepository.findByUser(testUser)).thenReturn(Optional.of(testUserTempReset));

    String expectedVerificationLink = frontUrl + "/user/recover/" + testToken;

    // when
    userService.recover(requestDto);

    // then
    verify(mailService).sendEmail(testEmail, "비밀번호 초기화 링크입니다", expectedVerificationLink);
  }

  @Test
  void recover_SUCCESS_UserTempResetNotExist() {
    // given
    RecoverRequestDto requestDto = RecoverRequestDto.builder()
        .email(testEmail)
        .build();
    String email = requestDto.getEmail();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
    when(userTempResetRepository.findByUser(testUser)).thenReturn(
        Optional.empty());

    when(uuidGenerator.generateUUID()).thenReturn(testToken);
    String expectedVerificationLink = frontUrl + "/user/recover/" + testToken;

    // when
    userService.recover(requestDto);

    // then
    verify(userTempResetRepository, times(1)).save(any(UserTempReset.class));
    verify(mailService).sendEmail(email, "비밀번호 초기화 링크입니다", expectedVerificationLink);
  }

  @Test
  void recover_UserNotExist() {
    // given
    RecoverRequestDto requestDto = RecoverRequestDto.builder()
        .email(testEmail)
        .build();
    String email = requestDto.getEmail();

    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    // when
    userService.recover(requestDto);

    // then
    verify(mailService).sendEmail(email, "비밀번호 초기화 링크입니다", "해당 이메일로 가입된 정보가 없습니다");
  }

  @Test
  void recoverEmailSend_SUCCESS() {
    // given
    String email = testEmail;
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
    when(userTempResetRepository.findByUser(testUser)).thenReturn(Optional.of(testUserTempReset));

    String expectedVerificationLink = frontUrl + "/user/recover/" + testToken;

    // when
    userService.recoverEmailSend(email);

    // then
    verify(mailService).sendEmail(email, "비밀번호 초기화 링크입니다", expectedVerificationLink);
  }

  @Test
  void recoverEmailSend_USER_UNREGISTERED() {
    // given
    String email = testEmail;
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    // when
    UserException exception = assertThrows(UserException.class,
        () -> userService.recoverEmailSend(email));

    // then
    assertEquals(USER_UNREGISTERED, exception.getId());
  }

  @Test
  void testRecoverEmailSend_UserTempResetNotExist() {
    // given
    String email = testEmail;
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
    when(userTempResetRepository.findByUser(testUser)).thenReturn(Optional.empty());

    // when
    UserException exception = assertThrows(UserException.class,
        () -> userService.recoverEmailSend(email));

    // then
    assertEquals(USER_UNREGISTERED, exception.getId());
  }

  @Test
  void recoverTokenCheck_SUCCESS() {
    // given
    String token = testToken;
    when(userTempResetRepository.findByToken(token)).thenReturn(Optional.of(testUserTempReset));

    // when
    RecoverTokenCheckResponseDto responseDto = userService.recoverTokenCheck(token);

    // then
    assertEquals(testEmail, responseDto.getEmail());
  }

  @Test
  void recoverTokenCheck_USER_UNREGISTERED() {
    // given
    String token = testToken;
    when(userTempResetRepository.findByToken(token)).thenReturn(Optional.empty());

    // when
    UserException exception = assertThrows(UserException.class,
        () -> userService.recoverTokenCheck(token));

    // then
    assertEquals(USER_UNREGISTERED, exception.getId());
  }

  @Test
  void recoverConfirm_SUCCESS() {
    // given
    RecoverConfirmRequestDto requestDto = RecoverConfirmRequestDto.builder()
        .token(testToken)
        .email(testEmail)
        .password(testDiffPassword)
        .build();
    String token = requestDto.getToken();
    String email = requestDto.getEmail();
    String diffPassword = requestDto.getPassword();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
    when(userTempResetRepository.findByToken(token)).thenReturn(Optional.of(testUserTempReset));

    when(passwordEncoder.encode(diffPassword)).thenReturn("encodedPassword");
    when(passwordEncoder.matches(diffPassword, "encodedPassword")).thenReturn(true);

    // when
    userService.recoverConfirm(requestDto);

    // then
    Optional<User> changedPasswordUser = userRepository.findByEmail(testEmail);
    verify(passwordEncoder, times(1)).encode(diffPassword);
    verify(userRepository, times(1)).save(any(User.class));
    verify(userTempResetRepository, times(1)).delete(any(UserTempReset.class));
    assertNotNull(changedPasswordUser);
    changedPasswordUser.ifPresent(
        user -> assertTrue(user.checkPassword(diffPassword, passwordEncoder)));
  }

  @Test
  void recoverConfirm_USER_UNREGISTERED() {
    // given
    RecoverConfirmRequestDto requestDto = RecoverConfirmRequestDto.builder()
        .token(testToken)
        .email(testEmail)
        .password(testDiffPassword)
        .build();
    String email = requestDto.getEmail();

    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    // when
    UserException exception = assertThrows(UserException.class,
        () -> userService.recoverConfirm(requestDto));

    // then
    assertEquals(USER_UNREGISTERED, exception.getId());
  }

  @Test
  void testRecoverConfirm_UserTempResetNotExist() {
    // given
    RecoverConfirmRequestDto requestDto = RecoverConfirmRequestDto.builder()
        .token(testToken)
        .email(testEmail)
        .password(testDiffPassword)
        .build();
    String email = testEmail;
    String token = testToken;

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
    when(userTempResetRepository.findByToken(token)).thenReturn(Optional.empty());

    // when
    UserException exception = assertThrows(UserException.class,
        () -> userService.recoverConfirm(requestDto));

    // then
    assertEquals(USER_UNREGISTERED, exception.getId());
  }

  @Test
  void recoverConfirm_TOKEN_INVALID() {
    // given
    RecoverConfirmRequestDto requestDto = RecoverConfirmRequestDto.builder()
        .token(testToken)
        .email(testEmail)
        .password(testDiffPassword)
        .build();

    // when
    UserException exception = assertThrows(UserException.class,
        () -> userService.recoverConfirm(requestDto));

    // then
    assertEquals(USER_UNREGISTERED, exception.getId());
  }

  @Test
  void resetPassword_SUCCESS() {
    // given
    testUserDetails();
    String email = testEmail;
    ResetPasswordRequestDto requestDto = ResetPasswordRequestDto.builder()
        .prevPassword(testPassword)
        .newPassword(testDiffPassword)
        .build();
    String prevPassword = requestDto.getPrevPassword();
    String newPassword = requestDto.getNewPassword();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(prevPassword, testPassword)).thenReturn(true);
    when(passwordEncoder.encode(newPassword)).thenReturn("encodedPassword");
    when(passwordEncoder.matches(newPassword, "encodedPassword")).thenReturn(true);

    // when
    userService.resetPassword(requestDto);

    // then
    Optional<User> changedPasswordUser = userRepository.findByEmail(testEmail);
    verify(passwordEncoder, times(1)).encode(newPassword);
    verify(userRepository, times(1)).save(any(User.class));
    assertNotNull(changedPasswordUser);
    changedPasswordUser.ifPresent(
        user -> assertTrue(user.checkPassword(newPassword, passwordEncoder)));
  }

  @Test
  void resetPassword_USER_UNREGISTERED() {
    // given
    testUserDetails();
    String email = testEmail;
    ResetPasswordRequestDto requestDto = ResetPasswordRequestDto.builder()
        .prevPassword(testPassword)
        .newPassword(testDiffPassword)
        .build();

    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    // when
    UserException exception = assertThrows(UserException.class,
        () -> userService.resetPassword(requestDto));

    // then
    assertEquals(USER_UNREGISTERED, exception.getId());
  }

  @Test
  void resetPassword_PASSWORD_MISMATCH() {
    // given
    testUserDetails();
    String email = testEmail;
    ResetPasswordRequestDto requestDto = ResetPasswordRequestDto.builder()
        .prevPassword(testPassword)
        .newPassword(testDiffPassword)
        .build();
    String prevPassword = requestDto.getPrevPassword();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(prevPassword, testPassword)).thenReturn(false);

    // when
    UserException exception = assertThrows(UserException.class,
        () -> userService.resetPassword(requestDto));

    // then
    assertEquals(PASSWORD_MISMATCH, exception.getId());
  }

  @Test
  void resetUsername_SUCCESS() {
    // given
    testUserDetails();
    String email = testEmail;
    ResetUsernameRequestDto requestDto = ResetUsernameRequestDto.builder()
        .username("diffUsername")
        .build();
    String diffUsername = requestDto.getUsername();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

    // when
    userService.resetUsername(requestDto);

    // then
    verify(userRepository, times(1)).save(testUser);
    assertTrue(testUser.checkUsername(diffUsername));
  }

  @Test
  void resetUsername_USER_UNREGISTERED() {
    // given
    testUserDetails();
    String email = testEmail;
    ResetUsernameRequestDto requestDto = ResetUsernameRequestDto.builder()
        .username("diffUsername")
        .build();

    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    // when
    UserException exception = assertThrows(UserException.class,
        () -> userService.resetUsername(requestDto));

    // then
    assertEquals(USER_UNREGISTERED, exception.getId());
  }

  @Test
  void resetUsername_USERNAME_EXIST() {
    // given
    String email = "test2@test.com";
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(userDetails.getUsername()).thenReturn(email);

    User user = User.builder()
        .email(email)
        .username("diffUsername")
        .password(passwordEncoder.encode(testPassword))
        .registerDate(LocalDateTime.MIN)
        .activated(true)
        .build();

    ResetUsernameRequestDto requestDto = ResetUsernameRequestDto.builder()
        .username(testUsername)
        .build();
    String username = requestDto.getUsername();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

    // when
    UserException exception = assertThrows(UserException.class,
        () -> userService.resetUsername(requestDto));

    // then
    assertEquals(USERNAME_EXIST, exception.getId());
  }

  @Test
  void userDelete_SUCCESS() {
    // given
    testUserDetails();
    String email = testEmail;
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

    // when
    userService.userDelete();

    // then
    verify(userRepository, times(1)).delete(testUser);
  }

  @Test
  void userDelete_USER_UNREGISTERED() {
    // given
    testUserDetails();
    String email = testEmail;
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    // when
    UserException exception = assertThrows(UserException.class,
        () -> userService.userDelete());

    // then
    assertEquals(USER_UNREGISTERED, exception.getId());
  }
}