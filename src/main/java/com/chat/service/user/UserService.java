package com.chat.service.user;

import com.chat.domain.user.Role;
import com.chat.domain.user.User;
import com.chat.domain.user.UserFriend;
import com.chat.domain.user.UserFriendTemp;
import com.chat.domain.user.UserRole;
import com.chat.domain.user.UserTemp;
import com.chat.domain.user.UserTempReset;
import com.chat.dto.JwtTokenDto;
import com.chat.dto.MessageDto;
import com.chat.dto.MessageDto.MessageType;
import com.chat.dto.user.ChangeAvatarRequestDto;
import com.chat.dto.user.ChangeAvatarResponseDto;
import com.chat.dto.user.ChangePasswordRequestDto;
import com.chat.dto.user.ChangeUsernameRequestDto;
import com.chat.dto.user.FriendAcceptRequestDto;
import com.chat.dto.user.FriendDeleteRequestDto;
import com.chat.dto.user.FriendListResponseDto;
import com.chat.dto.user.FriendRejectRequestDto;
import com.chat.dto.user.FriendRequestDto;
import com.chat.dto.user.FriendWaitingListResponseDto;
import com.chat.dto.user.LoginRequestDto;
import com.chat.dto.user.ProfileResponseDto;
import com.chat.dto.user.RecoverConfirmRequestDto;
import com.chat.dto.user.RecoverRequestDto;
import com.chat.dto.user.RecoverTokenCheckResponseDto;
import com.chat.dto.user.RegisterConfirmRequestDto;
import com.chat.dto.user.RegisterRequestDto;
import com.chat.dto.user.RegisterTokenCheckResponseDto;
import com.chat.dto.user.UserDeleteRequestDto;
import com.chat.dto.user.UserInfoForFriendListResponseDto;
import com.chat.dto.user.UserInfoForFriendWaitingListResponseDto;
import com.chat.exception.UserException;
import com.chat.jwt.TokenProvider;
import com.chat.jwt.TokenProvider.TokenType;
import com.chat.repository.server.ServerUserRelationRepository;
import com.chat.repository.user.RoleRepository;
import com.chat.repository.user.UserFriendRepository;
import com.chat.repository.user.UserFriendTempRepository;
import com.chat.repository.user.UserRepository;
import com.chat.repository.user.UserRoleRepository;
import com.chat.repository.user.UserTempRepository;
import com.chat.repository.user.UserTempResetRepository;
import com.chat.service.MailService;
import com.chat.util.UUIDGenerator;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

  private final CustomUserDetailsService customUserDetailsService;
  private final MailService mailService;

  private final UserRepository userRepository;
  private final UserRoleRepository userRoleRepository;
  private final UserTempRepository userTempRepository;
  private final UserTempResetRepository userTempResetRepository;
  private final RoleRepository roleRepository;
  private final UserFriendRepository userFriendRepository;
  private final UserFriendTempRepository userFriendTempRepository;
  private final ServerUserRelationRepository serverUserRelationRepository;

  private final AuthenticationManagerBuilder authenticationManagerBuilder;
  private final TokenProvider tokenProvider;
  private final PasswordEncoder passwordEncoder;
  private final UUIDGenerator uuidGenerator;

  private static final String EMAIL_EXIST = "USER:EMAIL_EXIST";
  private static final String USERNAME_EXIST = "USER:USERNAME_EXIST";
  private static final String USER_UNREGISTERED = "USER:USER_UNREGISTERED";
  private static final String PASSWORD_MISMATCH = "USER:PASSWORD_MISMATCH";
  private static final String TOKEN_INVALID = "USER:TOKEN_INVALID";
  private static final String EMAIL_OR_PASSWORD_ERROR = "USER:EMAIL_OR_PASSWORD_ERROR";
  private static final String EMAIL_ACTIVATE_REQUIRE = "USER:EMAIL_ACTIVATE_REQUIRE";
  private static final String USER_NOT_FOUND = "USER:USER_NOT_FOUND";
  private static final String IMAGE_INVALID = "USER:IMAGE_INVALID";
  private static final String IMAGE_SAVE_ERROR = "USER:IMAGE_SAVE_ERROR";
  private static final String USER_ALREADY_FRIEND = "USER:USER_ALREADY_FRIEND";
  private static final String USER_ALREADY_SENT_REQUEST = "USER:USER_ALREADY_SENT_REQUEST";
  private static final String USER_FRIEND_TEMP_NOT_FOUND = "USER:USER_FRIEND_TEMP_NOT_FOUND";

  private final SimpMessagingTemplate messagingTemplate;
  private static final String SUB_USER = "/sub/user/";
  private static final String SUB_SERVER = "/sub/server/";

  @Value("${server.front-url}")
  private String frontUrl;
  @Value("${server.pepper}")
  private String pepper;
  @Value("${server.file-path.user.image.avatar}")
  private String filePathUserImageAvatar;
  @Value("${server.time-zone}")
  private String timeZone;


  // 가입시 해당 이메일로 가입된 유저가 있는지 체크
  public Boolean validEmailDuplicate(String email) {
    Optional<User> user = userRepository.findByEmailAndLogicDeleteFalse(email);

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
    Optional<User> user = userRepository.findByUsernameAndLogicDeleteFalse(username);

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
        .password(passwordEncoder.encode(password + pepper))
        .registerDate(registerDate)
        .activated(false)
        .logicDelete(false)
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

    String verificationLink = frontUrl + "/register/confirm/" + token;
    mailService.sendEmail(email, "이메일 인증 메일 입니다", verificationLink);

    roleRepository.save(role);
    userRepository.save(user);
    userRoleRepository.save(userRole);
    userTempRepository.save(userTemp);
  }

  // 이메일 발송
  public void registerEmailSend(String email) {
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));
    UserTemp userTemp = userTempRepository.findByUser(user)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    // 이메일 재발송을 위한 토큰
    String token = userTemp.tokenForResend();
    String verificationLink = frontUrl + "/register/confirm/" + token;

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
  public void registerConfirm(RegisterConfirmRequestDto requestDto) {
    String token = requestDto.getToken();
    String email = requestDto.getEmail();

    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));
    UserTemp userTemp = userTempRepository.findByToken(token)
        .orElseThrow(() -> new UserException(TOKEN_INVALID));

    user.activate();

    userRepository.save(user);
    userTempRepository.delete(userTemp);
  }

  @Transactional
  public JwtTokenDto login(LoginRequestDto requestDto) {
    String email = requestDto.getEmail();
    String password = requestDto.getPassword();
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    if (!user.checkActivated()) {
      throw new UserException(EMAIL_ACTIVATE_REQUIRE);
    }

    try {
      // 1. Login Email, Password 로 AuthenticationToken 생성
      UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
          email, password + pepper);

      // 2. Email, Password 일치 검증이 일어남
      //    authenticationManagerBuilder.getObject().authenticate() 에서 CustomUserDetailsService 의 loadUserByUsername() 실행됨
      Authentication authentication = authenticationManagerBuilder.getObject()
          .authenticate(authenticationToken);
      SecurityContextHolder.getContext().setAuthentication(authentication);

      // 유저가 참여중인 서버의 id 리스트
      List<Long> serverIdList = serverUserRelationRepository
          .fetchServerIdListByUserAndServerDeleteFalseAndLogicDeleteFalse(user);

      // 3. 인증 정보로 JWT 토큰 생성
      // 4. 토큰 발급
      Long userId = user.fetchUserIdForCreateToken();
      String accessToken = tokenProvider.createToken(
          authentication,
          TokenType.ACCESS_TOKEN,
          userId);
      String refreshToken = tokenProvider.createToken(
          authentication,
          TokenType.REFRESH_TOKEN,
          userId);

      // 유저 온라인표시
      LocalDateTime lastLogin = LocalDateTime.now();
      user.updateOnline(lastLogin);
      userRepository.save(user);
      // 해당 유저가 속한 서버에 온라인메시지발송
      serverIdList.forEach(
          serverId -> {
            String serverUrl = SUB_SERVER + serverId;
            MessageDto newMessageDto = MessageDto.builder()
                .messageType(MessageType.USER_ONLINE)
                .serverId(serverId)
                .userId(userId)
                .build();
            messagingTemplate.convertAndSend(serverUrl, newMessageDto);
          }
      );

      return JwtTokenDto.builder()
          .accessToken(accessToken)
          .refreshToken(refreshToken)
          .build();
    } catch (Exception e) {
      throw new UserException(EMAIL_OR_PASSWORD_ERROR);
    }
  }

  // refresh -> lastLogin 갱신
  @Transactional
  public void refresh(String refreshToken) {
    Long userIdFromToken = tokenProvider.getUserIdFromToken(refreshToken);
    User user = userRepository.findByIdAndLogicDeleteFalse(userIdFromToken)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));
    LocalDateTime lastLogin = LocalDateTime.now();
    user.updateOnline(lastLogin);
    userRepository.save(user);
  }

  // 사용자정보 fetch
  public ProfileResponseDto profile() {
    String email = customUserDetailsService.getEmailByUserDetails();
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    // user 접속중으로 업데이트
    LocalDateTime now = LocalDateTime.now();
    user.updateOnline(now);
    userRepository.save(user);

    // user가 속해있는 서버에 접속알림
    Long userId = user.fetchUserIdForLoginAlert();
    List<Long> serverIdList = serverUserRelationRepository
        .fetchServerIdListByUserAndServerDeleteFalseAndLogicDeleteFalse(user);
    serverIdList.forEach(serverId -> CompletableFuture.runAsync(() -> {
      String serverUrl = SUB_SERVER + serverId;
      MessageDto messageDto = MessageDto.builder()
          .messageType(MessageType.USER_ONLINE)
          .serverId(serverId)
          .userId(userId)
          .build();
      messagingTemplate.convertAndSend(serverUrl, messageDto);
    }));

    return user.buildProfileResponseDto(filePathUserImageAvatar);
  }

  // 비밀번호 분실히 비밀번호 찾기
  @Transactional
  public void recover(RecoverRequestDto recoverRequestDto) {
    String email = recoverRequestDto.getEmail();

    // 해당 이메일로 가입된 유저가 없는경우
    User user = userRepository.findByEmailAndLogicDeleteFalse(email).orElse(null);
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
    String recoverUrl = frontUrl + "/recover/" + token;
    mailService.sendEmail(email, "비밀번호 초기화 링크입니다", recoverUrl);
  }

  // 비밀번호 분실시 발송했던 이메일을 재발송
  public void recoverEmailSend(String email) {
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));
    UserTempReset userTempReset = userTempResetRepository.findByUser(user)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));
    String token = userTempReset.tokenForResend();
    String verificationLink = frontUrl + "/recover/" + token;
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
  public void recoverConfirm(RecoverConfirmRequestDto requestDto) {
    String token = requestDto.getToken();
    String email = requestDto.getEmail();
    String password = passwordEncoder.encode(requestDto.getPassword() + pepper);

    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));
    UserTempReset userTempReset = userTempResetRepository.findByToken(token)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    // 비밀번호 복구 재설정 userTemp.user -> user.password
    userTempReset.recoverPassword(password);

    userRepository.save(user);
    userTempResetRepository.delete(userTempReset);
  }

  // 사용자명 재설정
  @Transactional
  public void changeUsername(ChangeUsernameRequestDto requestDto) {
    String email = customUserDetailsService.getEmailByUserDetails();
    String username = requestDto.getUsername();
    // 유저검색
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    // 사용자명 중복 검사
    validUsernameDuplicate(username);

    // 사용자명 재설정
    user.changeUsername(username);
    userRepository.save(user);

    // 바뀐 사용자명 각 서버에 메시지 전송
    // 유저가 참여중인 서버의 id 리스트
    Long userId = requestDto.getId();
    List<Long> serverIdList = serverUserRelationRepository
        .fetchServerIdListByUserAndServerDeleteFalseAndLogicDeleteFalse(user);
    serverIdList.forEach(
        serverId -> {
          String serverUrl = SUB_SERVER + serverId;
          MessageDto newMessageDto = MessageDto.builder()
              .messageType(MessageType.USER_UPDATE_USERNAME)
              .serverId(serverId)
              .userId(userId)
              .username(username)
              .build();
          messagingTemplate.convertAndSend(serverUrl, newMessageDto);
        }
    );
  }

  // 아바타이미지 재설정
  @Transactional
  public ChangeAvatarResponseDto changeAvatar(ChangeAvatarRequestDto requestDto)
      throws IOException {
    String email = customUserDetailsService.getEmailByUserDetails();
    // 유저검색
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    String avatar = requestDto.getAvatar();
    // base64 데이터 검증 및 분리
    if (avatar == null || !avatar.contains(",")) {
      throw new UserException(IMAGE_INVALID);
    }
    String[] base64 = avatar.split(",");
    String metadata = base64[0];  // data:image/png;base64
    String base64Data = base64[1];  // 실제 base64 데이터
    String mimeType = metadata.split(":")[1].split(";")[0]; // image/png

    // 이미지 확장자 추출
    String extension = getFileExtensionFromMimeType(mimeType);
    if (extension == null) {
      throw new UserException(IMAGE_INVALID);
    }

    // base64 데이터를 바이트 배열로 디코딩
    byte[] decode = Base64.getDecoder().decode(base64Data);

    // 현재 시간 millisecond
    ZoneId zoneid = ZoneId.of(timeZone);
    long epochMilli = LocalDateTime.now().atZone(zoneid).toInstant().toEpochMilli();

    String fileName = uuidGenerator.generateUUID() + "_" + epochMilli;
    String fileNameSmall = fileName + "_small." + extension;
    String fileNameLarge = fileName + "_large." + extension;

    String filePathSmall = filePathUserImageAvatar + fileNameSmall;
    String filePathLarge = filePathUserImageAvatar + fileNameLarge;

    BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(decode));

    // 폴더없는경우 생성
    Files.createDirectories(Paths.get(filePathUserImageAvatar));

    try {
      // 이미지 리사이징 후 저장 (작은 이미지)
      Thumbnails.of(originalImage)
          .size(32, 32)
          .toFile(new File(filePathSmall));

      // 이미지 리사이징 후 저장 (큰 이미지)
      Thumbnails.of(originalImage)
          .size(100, 100)
          .toFile(new File(filePathLarge));
    } catch (IOException e) {
      throw new UserException(IMAGE_SAVE_ERROR);
    }

    // 새로운 이미지 경로 저장
    user.changeAvatar(filePathSmall, filePathLarge);
    userRepository.save(user);

    // 유저가 속해있는 serverList 에 메시지 발행
    Long userId = user.fetchUserIdForChangeAvatar();
    List<Long> serverIdList = serverUserRelationRepository
        .fetchServerIdListByUserAndServerDeleteFalseAndLogicDeleteFalse(user);
    serverIdList.forEach(serverId -> CompletableFuture.runAsync(() -> {
      String serverUrl = SUB_SERVER + serverId;
      MessageDto messageDto = MessageDto.builder()
          .messageType(MessageType.USER_UPDATE_AVATAR)
          .serverId(serverId)
          .userId(userId)
          .message(filePathSmall)
          .build();
      messagingTemplate.convertAndSend(serverUrl, messageDto);
    }));

    return ChangeAvatarResponseDto.builder()
        .avatarImageSmall(filePathSmall)
        .build();
  }

  // base64 문자열에서 이미지 확장자 추출
  private String getFileExtensionFromMimeType(String mimeType) {
    if (mimeType.startsWith("image/jpeg")) {
      return "jpg";
    } else if (mimeType.startsWith("image/png")) {
      return "png";
    } else if (mimeType.startsWith("image/gif")) {
      return "gif";
    } else {
      return null;  // 지원하지 않는 파일 형식
    }
  }

  // 비밀번호 재설정
  @Transactional
  public void changePassword(ChangePasswordRequestDto requestDto) {
    String email = customUserDetailsService.getEmailByUserDetails();
    String prevPassword = requestDto.getPrevPassword();
    String newPassword = passwordEncoder.encode(requestDto.getNewPassword() + pepper);

    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));
    // 비밀번호 재설정시 이전 비밀번호 확인
    if (user.checkPassword(prevPassword + pepper, passwordEncoder)) {
      user.changePassword(newPassword);
      userRepository.save(user);
      return;
    }
    throw new UserException(PASSWORD_MISMATCH);
  }

  // 유저 삭제
  @Transactional
  public void userDelete(UserDeleteRequestDto requestDto) {
    String email = customUserDetailsService.getEmailByUserDetails();

    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    String password = requestDto.getPassword();
    if (user.checkPassword(password + pepper, passwordEncoder)) {
      user.logicDelete();
      userRepository.save(user);
      return;
    }
    throw new UserException(PASSWORD_MISMATCH);
  }

  // 친구 신청
  @Transactional
  public void friend(FriendRequestDto requestDto) {
    String email = customUserDetailsService.getEmailByUserDetails();

    // 유저검색
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    String friendName = requestDto.getFriendName();
    User friend = userRepository.findByUsernameAndLogicDeleteFalse(friendName)
        .orElseThrow(() -> new UserException(USER_NOT_FOUND));

    // 이미 친구인 경우 오류
    if (userFriendRepository.fetchByUserAndFriend(user, friend).isPresent()) {
      throw new UserException(USER_ALREADY_FRIEND);
    }
    // 이미 신청하였다면 다시 메시지 보내지않음
    if (userFriendTempRepository.fetchByUserAndFriend(user, friend).isPresent()) {
      throw new UserException(USER_ALREADY_SENT_REQUEST);
    }

    // 새로운 요청 저장 및 상대방에게 알림 전송
    UserFriendTemp userFriendTemp = UserFriendTemp.builder()
        .user(user)
        .friend(friend)
        .build();
    userFriendTempRepository.save(userFriendTemp);

    // id, username -> 요청보내는사람
    // friendId -> 요청받는사람
    Long friendId = friend.fetchUserIdForFriendRequest();
    String username = requestDto.getUsername();
    String userUrl = SUB_USER + friendId;
    Long id = requestDto.getId();
    // 요청 보내는사람의 정보(id)를 요청받는사람(friendId)에게 보냄
    MessageDto newMessageDto = MessageDto.builder()
        .messageType(MessageType.FRIEND_REQUEST)
        .userId(id)
        .username(username)
        .build();
    messagingTemplate.convertAndSend(userUrl, newMessageDto);
  }

  // 친구 요청 대기중인 정보 가져오기
  public FriendWaitingListResponseDto friendWaitingList() {
    String email = customUserDetailsService.getEmailByUserDetails();

    // 유저검색
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));
    List<UserInfoForFriendWaitingListResponseDto> waitingList = userFriendTempRepository
        .fetchUserInfoByUser(user);

    return FriendWaitingListResponseDto.builder()
        .waitingList(waitingList)
        .build();
  }

  // 친구 리스트 정보 가져오기
  public FriendListResponseDto friendList() {
    String email = customUserDetailsService.getEmailByUserDetails();
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    List<UserInfoForFriendListResponseDto> userInfoList = userFriendRepository
        .fetchUserInfoDtoListByUser(user);
    return FriendListResponseDto.builder()
        .friendList(userInfoList)
        .build();
  }

  // 친구신청 수락
  @Transactional
  public void friendAccept(FriendAcceptRequestDto requestDto) {
    String email = customUserDetailsService.getEmailByUserDetails();
    Long friendId = requestDto.getFriendId();
    // 유저검색
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));
    User friend = userRepository.findByIdAndLogicDeleteFalse(friendId)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    // 존재하는 요청인지 확인
    UserFriendTemp userFriendTemp = userFriendTempRepository.fetchByUserAndFriend(user, friend)
        .orElseThrow(() -> new UserException(USER_FRIEND_TEMP_NOT_FOUND));

    // 등록
    UserFriend userFriend = UserFriend.builder()
        .user(user)
        .friend(friend)
        .build();
    UserFriend friendUser = UserFriend.builder()
        .user(friend)
        .friend(user)
        .build();

    userFriendTempRepository.delete(userFriendTemp);
    userFriendRepository.save(userFriend);
    userFriendRepository.save(friendUser);

    // id, username -> 요청 수락한 유저
    // friendId -> 요청 신청했던 유저
    Long id = requestDto.getId();
    String username = requestDto.getUsername();
    String userUrl = SUB_USER + friendId;
    // 요청 수락한 유저(id)의 정보를 요청 신청했던유저(friendId)가 받아서 상태갱신
    MessageDto newMessageDto = MessageDto.builder()
        .messageType(MessageType.FRIEND_ACCEPT)
        .userId(id)
        .username(username)
        .build();
    messagingTemplate.convertAndSend(userUrl, newMessageDto);
  }

  // 친구신청 거절
  @Transactional
  public void friendReject(FriendRejectRequestDto requestDto) {
    String email = customUserDetailsService.getEmailByUserDetails();
    Long id = requestDto.getId();

    // 유저검색
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));
    User friend = userRepository.findByIdAndLogicDeleteFalse(id)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    // 존재하는 요청인지 확인
    UserFriendTemp userFriendTemp = userFriendTempRepository.fetchByUserAndFriend(user, friend)
        .orElseThrow(() -> new UserException(USER_FRIEND_TEMP_NOT_FOUND));

    // 삭제
    userFriendTempRepository.delete(userFriendTemp);
  }

  // 친구 삭제
  @Transactional
  public void friendDelete(FriendDeleteRequestDto requestDto) {
    String email = customUserDetailsService.getEmailByUserDetails();
    Long friendId = requestDto.getFriendId();

    // 유저검색
    User user = userRepository.findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));
    User friend = userRepository.findByIdAndLogicDeleteFalse(friendId)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    // 삭제
    userFriendRepository.deleteAll(userFriendRepository.fetchListByUserAndFriend(user, friend));

    // id, username -> 친구삭제 요청을 보낸유저
    // friendId -> 친구삭제 대상
    Long id = requestDto.getId();
    String username = requestDto.getUsername();
    String userUrl = SUB_USER + friendId;
    // 친구삭제대상(friendId)에게 친구삭제 요청을 보낸유저(id)의 정보를 보냄
    MessageDto newMessageDto = MessageDto.builder()
        .messageType(MessageType.FRIEND_DELETE)
        .userId(id)
        .username(username)
        .build();
    messagingTemplate.convertAndSend(userUrl, newMessageDto);
  }
}
