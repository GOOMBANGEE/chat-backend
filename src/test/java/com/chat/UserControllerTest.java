package com.chat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chat.controller.UserController;
import com.chat.dto.AccessTokenDto;
import com.chat.dto.JwtTokenDto;
import com.chat.dto.user.EmailCheckRequestDto;
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
import com.chat.dto.user.UsernameCheckRequestDto;
import com.chat.exception.GlobalExceptionHandler;
import com.chat.exception.UserException;
import com.chat.jwt.TokenProvider;
import com.chat.service.user.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;

@WebMvcTest(UserController.class)
@ExtendWith({SpringExtension.class, MockitoExtension.class})
class UserControllerTest {

  @Autowired
  private UserController userController;

  @MockBean
  private UserService userService;

  @MockBean
  private TokenProvider tokenProvider;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  private static final String EMAIL_EXIST = "USER:EMAIL_EXIST";
  private static final String USERNAME_EXIST = "USER:USERNAME_EXIST";
  private static final String USER_UNREGISTERED = "USER:USER_UNREGISTERED";
  private static final String PASSWORD_MISMATCH = "USER:PASSWORD_MISMATCH";
  private static final String TOKEN_INVALID = "USER:TOKEN_INVALID";

  private String testEmail;
  private String testUsername;
  private String testPassword;
  private String testDiffPassword;
  private String testToken;
  private String testAccessToken;
  private String testRefreshToken;

  @BeforeEach
  void setUp() {
    testEmail = "test@test.com";
    testUsername = "test";
    testPassword = "1q2w3e4r!";
    testDiffPassword = "diffPassword";
    testAccessToken = "testAccessToken";
    testRefreshToken = "testRefreshToken";
    testToken = "test";

    mockMvc = MockMvcBuilders
        .standaloneSetup(userController)
        .setValidator(mock(Validator.class))
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  private String toJson(Object dto) throws JsonProcessingException {
    return objectMapper.writeValueAsString(dto);
  }

  @Test
  void testEmailCheck_SUCCESS() throws Exception {
    // given
    EmailCheckRequestDto requestDto = EmailCheckRequestDto.builder()
        .email(testEmail)
        .build();
    String email = requestDto.getEmail();
    when(userService.validEmailDuplicate(email)).thenReturn(true);

    // when
    ResultActions actions = mockMvc.perform(post("/api/user/email/check")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)));

    // then
    actions.andExpect(status().isOk())
        .andExpect(content().string("true"))
        .andDo(print());
  }

  @Test
  void testEmailCheck_EMAIL_EXIST() throws Exception {
    // given
    EmailCheckRequestDto requestDto = EmailCheckRequestDto.builder()
        .email(testEmail)
        .build();
    String email = requestDto.getEmail();
    when(userService.validEmailDuplicate(email))
        .thenThrow(new UserException(EMAIL_EXIST));

    // when
    ResultActions actions = mockMvc.perform(post("/api/user/email/check")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)));

    // then
    actions.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.id").value(EMAIL_EXIST))
        .andDo(print());
  }

  @Test
  void testUsernameCheck_SUCCESS() throws Exception {
    // given
    UsernameCheckRequestDto requestDto = UsernameCheckRequestDto.builder()
        .username(testUsername)
        .build();
    String username = requestDto.getUsername();
    when(userService.validUsernameDuplicate(username)).thenReturn(true);

    // when
    ResultActions actions = mockMvc.perform(post("/api/user/username/check")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)));

    // then
    actions.andExpect(status().isOk())
        .andExpect(content().string("true"))
        .andDo(print());
  }

  @Test
  void testUsernameCheck_USERNAME_EXIST() throws Exception {
    // given
    UsernameCheckRequestDto requestDto = UsernameCheckRequestDto.builder()
        .username(testUsername)
        .build();
    String username = requestDto.getUsername();
    when(userService.validUsernameDuplicate(username))
        .thenThrow(new UserException(USERNAME_EXIST));

    // when
    ResultActions actions = mockMvc.perform(post("/api/user/username/check")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)));

    // then
    actions.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.id").value(USERNAME_EXIST))
        .andDo(print());
  }

  @Test
  void testRegister_SUCCESS() throws Exception {
    // given
    RegisterRequestDto requestDto = RegisterRequestDto.builder()
        .email(testEmail)
        .username(testUsername)
        .password(testPassword)
        .confirmPassword(testPassword)
        .build();

    // when
    ResultActions actions = mockMvc.perform(post("/api/user/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)));

    // then
    actions.andExpect(status().isOk())
        .andDo(print());
  }

  @Test
  void testRegister_PASSWORD_MISMATCH() throws Exception {
    // given
    RegisterRequestDto requestDto = RegisterRequestDto.builder()
        .email(testEmail)
        .username(testUsername)
        .password(testPassword)
        .confirmPassword(testDiffPassword)
        .build();

    doThrow(new UserException(PASSWORD_MISMATCH)).when(userService)
        .register(any(RegisterRequestDto.class));

    // when
    ResultActions actions = mockMvc.perform(post("/api/user/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)));

    // then
    actions.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.id").value(PASSWORD_MISMATCH))
        .andDo(print());
  }

  @Test
  void testRegister_EMAIL_EXIST() throws Exception {
    // given
    RegisterRequestDto requestDto = RegisterRequestDto.builder()
        .email(testEmail)
        .username(testUsername)
        .password(testPassword)
        .confirmPassword(testPassword)
        .build();

    doThrow(new UserException(EMAIL_EXIST)).when(userService)
        .register(any(RegisterRequestDto.class));

    // when
    ResultActions actions = mockMvc.perform(post("/api/user/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)));

    // then
    actions.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.id").value(EMAIL_EXIST))
        .andDo(print());
  }

  @Test
  void testRegister_USERNAME_EXIST() throws Exception {
    // given
    RegisterRequestDto requestDto = RegisterRequestDto.builder()
        .email(testEmail)
        .username(testUsername)
        .password(testPassword)
        .confirmPassword(testPassword)
        .build();

    doThrow(new UserException(USERNAME_EXIST)).when(userService)
        .register(any(RegisterRequestDto.class));

    // when
    ResultActions actions = mockMvc.perform(post("/api/user/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)));

    // then
    actions.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.id").value(USERNAME_EXIST))
        .andDo(print());
  }

  @Test
  void testRegisterEmailSend_SUCCESS() throws Exception {
    // given
    RegisterEmailSendRequestDto requestDto = RegisterEmailSendRequestDto.builder()
        .email(testEmail)
        .build();

    // when
    ResultActions actions = mockMvc.perform(post("/api/user/register/email/send")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)));

    // then
    actions.andExpect(status().isOk())
        .andDo(print());
  }

  @Test
  void testRegisterEmailSend_USER_UNREGISTERED() throws Exception {
    // given
    RegisterEmailSendRequestDto requestDto = RegisterEmailSendRequestDto.builder()
        .email(testEmail)
        .build();
    String email = requestDto.getEmail();

    doThrow(new UserException(USER_UNREGISTERED)).when(userService).registerEmailSend(email);

    // when
    ResultActions actions = mockMvc.perform(post("/api/user/register/email/send")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)));

    // then
    actions.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.id").value(USER_UNREGISTERED))
        .andDo(print());
  }

  @Test
  void testRegisterTokenCheck_SUCCESS() throws Exception {
    // given
    String token = testToken;
    RegisterTokenCheckResponseDto responseDto = RegisterTokenCheckResponseDto.builder()
        .email(testEmail)
        .build();

    when(userService.registerTokenCheck(token)).thenReturn(responseDto);

    // when
    ResultActions actions = mockMvc.perform(get("/api/user/register/{token}", token)
        .contentType(MediaType.APPLICATION_JSON));

    // then
    actions.andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(testEmail))
        .andDo(print());
  }

  @Test
  void testRegisterTokenCheck_TOKEN_INVALID() throws Exception {
    // given
    String token = testToken;

    when(userService.registerTokenCheck(token)).thenThrow(new UserException(TOKEN_INVALID));

    // when
    ResultActions actions = mockMvc.perform(get("/api/user/register/{token}", token)
        .contentType(MediaType.APPLICATION_JSON));

    // then
    actions.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.id").value(TOKEN_INVALID))
        .andDo(print());
  }

  @Test
  void testRegisterConfirm_SUCCESS() throws Exception {
    // given
    RegisterConfirmRequestDto requestDto = RegisterConfirmRequestDto.builder()
        .token(testToken)
        .email(testEmail)
        .build();

    // when
    ResultActions actions = mockMvc.perform(post("/api/user/register/confirm")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)));

    // then
    actions.andExpect(status().isOk())
        .andDo(print());
  }

  @Test
  void testRegisterConfirm_USER_UNREGISTERED() throws Exception {
    // given
    RegisterConfirmRequestDto requestDto = RegisterConfirmRequestDto.builder()
        .token(testToken)
        .email(testEmail)
        .build();

    doThrow(new UserException(USER_UNREGISTERED)).when(userService)
        .registerConfirm(any(RegisterConfirmRequestDto.class));

    // when
    ResultActions actions = mockMvc.perform(post("/api/user/register/confirm")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)));

    // then
    actions
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.id").value(USER_UNREGISTERED))
        .andDo(print());
  }

  @Test
  void testRegisterConfirm_TOKEN_INVALID() throws Exception {
    // given
    RegisterConfirmRequestDto requestDto = RegisterConfirmRequestDto.builder()
        .token(testToken)
        .email(testEmail)
        .build();

    doThrow(new UserException(TOKEN_INVALID)).when(userService)
        .registerConfirm(any(RegisterConfirmRequestDto.class));

    // when
    ResultActions actions = mockMvc.perform(post("/api/user/register/confirm")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)));

    // then
    actions
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.id").value(TOKEN_INVALID))
        .andDo(print());
  }

  @Test
  void testLogin_SUCCESS() throws Exception {
    // given
    LoginRequestDto requestDto = LoginRequestDto.builder()
        .email(testEmail)
        .password(testPassword)
        .build();

    JwtTokenDto jwtTokenDto = JwtTokenDto.builder()
        .accessToken(testAccessToken)
        .refreshToken(testRefreshToken)
        .build();

    when(userService.login(any(LoginRequestDto.class))).thenReturn(jwtTokenDto);

    // when
    ResultActions actions = mockMvc.perform(post("/api/user/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)));

    // then
    actions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value(testAccessToken))
        .andExpect(jsonPath("$.refreshToken").value(testRefreshToken))
        .andDo(print());
  }

  @Test
  void testRefresh_SUCCESS() throws Exception {
    // given
    RefreshRequestDto requestDto = RefreshRequestDto.builder()
        .refreshToken(testRefreshToken)
        .build();
    String refreshToken = requestDto.getRefreshToken();

    AccessTokenDto accessTokenDto = AccessTokenDto.builder()
        .accessToken(testAccessToken)
        .build();

    when(tokenProvider.validateToken(refreshToken)).thenReturn(true);
    when(tokenProvider.refreshAccessToken(refreshToken)).thenReturn(accessTokenDto);

    // when
    ResultActions actions = mockMvc.perform(post("/api/user/refresh")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)));

    // then
    actions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value(testAccessToken))
        .andDo(print());
  }

  @Test
  void testRefresh_TOKEN_INVALID() throws Exception {
    // given
    RefreshRequestDto requestDto = RefreshRequestDto.builder()
        .refreshToken(testRefreshToken)
        .build();
    String refreshToken = requestDto.getRefreshToken();

    when(tokenProvider.validateToken(refreshToken)).thenReturn(false);

    // when
    ResultActions actions = mockMvc.perform(post("/api/user/refresh")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)));

    // then
    actions
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.id").value(TOKEN_INVALID))
        .andDo(print());
  }

  @Test
  void testProfile_SUCCESS() throws Exception {
    // given
    ProfileResponseDto responseDto = ProfileResponseDto.builder()
        .email(testEmail)
        .username(testUsername)
        .build();

    when(userService.profile()).thenReturn(responseDto);

    // when
    ResultActions actions = mockMvc.perform(get("/api/user/profile"));

    // then
    actions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(testEmail))
        .andExpect(jsonPath("$.username").value(testUsername))
        .andDo(print());
  }

  @Test
  void testProfile_USER_UNREGISTERED() throws Exception {
    // given
    doThrow(new UserException(USER_UNREGISTERED)).when(userService).profile();

    // when
    ResultActions actions = mockMvc.perform(get("/api/user/profile"));

    // then
    actions
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.id").value(USER_UNREGISTERED))
        .andDo(print());
  }

  @Test
  void testRecover_SUCCESS() throws Exception {
    // given
    RecoverRequestDto requestDto = RecoverRequestDto.builder().build();

    // when
    ResultActions actions = mockMvc.perform(post("/api/user/recover")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)));

    // then
    actions
        .andExpect(status().isOk())
        .andDo(print());
  }

  @Test
  void testRecoverEmailSend_SUCCESS() throws Exception {
    // given
    RecoverEmailSendRequestDto requestDto = RecoverEmailSendRequestDto.builder()
        .email(testEmail)
        .build();

    // when
    ResultActions actions = mockMvc.perform(post("/api/user/recover/email/send")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)));

    // then
    actions
        .andExpect(status().isOk())
        .andDo(print());
  }

  @Test
  void testRecoverEmailSend_USER_UNREGISTERED() throws Exception {
    // given
    RecoverEmailSendRequestDto requestDto = RecoverEmailSendRequestDto.builder()
        .email(testEmail)
        .build();
    String email = requestDto.getEmail();
    doThrow(new UserException(USER_UNREGISTERED)).when(userService).recoverEmailSend(email);

    // when
    ResultActions actions = mockMvc.perform(post("/api/user/recover/email/send")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)));

    // then
    actions
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.id").value(USER_UNREGISTERED))
        .andDo(print());
  }

  @Test
  void testRecoverTokenCheck_SUCCESS() throws Exception {
    // given
    String token = testToken;
    RecoverTokenCheckResponseDto responseDto = RecoverTokenCheckResponseDto.builder()
        .email(testEmail)
        .build();

    when(userService.recoverTokenCheck(token)).thenReturn(responseDto);

    // when
    ResultActions actions = mockMvc.perform(get("/api/user/recover/{token}", token));

    // then
    actions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(testEmail))
        .andDo(print());
  }

  @Test
  void testRecoverTokenCheck_USER_UNREGISTERED() throws Exception {
    // given
    String token = testToken;

    when(userService.recoverTokenCheck(token)).thenThrow(new UserException(USER_UNREGISTERED));

    // when
    ResultActions actions = mockMvc.perform(get("/api/user/recover/{token}", token));

    // then
    actions
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.id").value(USER_UNREGISTERED))
        .andDo(print());
  }

  @Test
  void testRecoverConfirm_SUCCESS() throws Exception {
    // given
    RecoverConfirmRequestDto requestDto = RecoverConfirmRequestDto.builder()
        .token(testToken)
        .email(testEmail)
        .password(testPassword)
        .build();

    // when
    ResultActions actions = mockMvc.perform(post("/api/user/recover/confirm")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)));

    // then
    actions
        .andExpect(status().isOk())
        .andDo(print());
  }

  @Test
  void testRecoverConfirm_USER_UNREGISTERED() throws Exception {
    // given
    RecoverConfirmRequestDto requestDto = RecoverConfirmRequestDto.builder()
        .token(testToken)
        .email(testEmail)
        .password(testPassword)
        .build();

    doThrow(new UserException(USER_UNREGISTERED)).when(userService)
        .recoverConfirm(any(RecoverConfirmRequestDto.class));

    // when
    ResultActions actions = mockMvc.perform(post("/api/user/recover/confirm")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)));

    // then
    actions
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.id").value(USER_UNREGISTERED))
        .andDo(print());
  }

  @Test
  void testResetPassword_SUCCESS() throws Exception {
    // given
    ResetPasswordRequestDto requestDto = ResetPasswordRequestDto.builder()
        .prevPassword(testPassword)
        .newPassword(testDiffPassword)
        .build();

    // when
    ResultActions actions = mockMvc.perform(post("/api/user/reset/password")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)));

    // then
    actions
        .andExpect(status().isOk())
        .andDo(print());
  }

  @Test
  void testResetPassword_USER_UNREGISTERED() throws Exception {
    // given
    ResetPasswordRequestDto requestDto = ResetPasswordRequestDto.builder()
        .prevPassword(testPassword)
        .newPassword(testDiffPassword)
        .build();

    doThrow(new UserException(USER_UNREGISTERED)).when(userService)
        .resetPassword(any(ResetPasswordRequestDto.class));

    // when
    ResultActions actions = mockMvc.perform(post("/api/user/reset/password")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)));

    // then
    actions
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.id").value(USER_UNREGISTERED))
        .andDo(print());
  }


  @Test
  void testResetPassword_PASSWORD_MISMATCH() throws Exception {
    // given
    ResetPasswordRequestDto requestDto = ResetPasswordRequestDto.builder()
        .prevPassword(testDiffPassword)
        .newPassword(testDiffPassword)
        .build();

    doThrow(new UserException(PASSWORD_MISMATCH)).when(userService)
        .resetPassword(any(ResetPasswordRequestDto.class));

    // when
    ResultActions actions = mockMvc.perform(post("/api/user/reset/password")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)));

    // then
    actions
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.id").value(PASSWORD_MISMATCH))
        .andDo(print());
  }

  @Test
  void testResetUsername_SUCCESS() throws Exception {
    // given
    ResetUsernameRequestDto requestDto = ResetUsernameRequestDto.builder()
        .username(testUsername)
        .build();

    // when
    ResultActions actions = mockMvc.perform(post("/api/user/reset/username")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)));

    // then
    actions
        .andExpect(status().isOk())
        .andDo(print());
  }

  @Test
  void testResetUsername_USER_UNREGISTERED() throws Exception {
    // given
    ResetUsernameRequestDto requestDto = ResetUsernameRequestDto.builder()
        .username(testUsername)
        .build();

    doThrow(new UserException(USER_UNREGISTERED)).when(userService)
        .resetUsername(any(ResetUsernameRequestDto.class));

    // when
    ResultActions actions = mockMvc.perform(post("/api/user/reset/username")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(requestDto)));

    // then
    actions
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.id").value(USER_UNREGISTERED))
        .andDo(print());
  }

  @Test
  void testUserDelete_SUCCESS() throws Exception {
    // when
    ResultActions actions = mockMvc.perform(delete("/api/user/delete"));

    // then
    actions
        .andExpect(status().isOk())
        .andDo(print());
  }

  @Test
  void testUserDelete_USER_UNREGISTERED() throws Exception {
    // given
    doThrow(new UserException(USER_UNREGISTERED)).when(userService).userDelete();

    // when
    ResultActions actions = mockMvc.perform(delete("/api/user/delete"));

    // then
    actions
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.id").value(USER_UNREGISTERED))
        .andDo(print());
  }
}
