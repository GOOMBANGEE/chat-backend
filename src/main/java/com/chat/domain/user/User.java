package com.chat.domain.user;

import com.chat.dto.user.ProfileResponseDto;
import com.chat.dto.user.UserInfoForServerJoinResponseDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "email", unique = true)
  private String email;

  @Column(name = "username", unique = true)
  private String username;

  private String password;

  private LocalDateTime registerDate;

  private boolean activated;

  private boolean logicDelete;

  private String avatarImageSmall;

  private String avatarImageLarge;
  // login, refresh 요청시 갱신
  private LocalDateTime lastLogin;

  private boolean online;

  @Builder
  public User(String email, String username, String password, LocalDateTime registerDate,
      boolean activated, boolean logicDelete, String avatarImageSmall, String avatarImageLarge) {
    this.email = email;
    this.username = username;
    this.password = password;
    this.registerDate = registerDate;
    this.activated = activated;
    this.logicDelete = logicDelete;
    this.avatarImageSmall = avatarImageSmall;
    this.avatarImageLarge = avatarImageLarge;
  }

  public org.springframework.security.core.userdetails.User buildUserDetails(
      List<SimpleGrantedAuthority> grantedAuthorities) {
    return new org.springframework.security.core.userdetails.User(this.email, this.password,
        grantedAuthorities);
  }

  // 활성화 되어있는지 확인
  public boolean checkActivated() {
    return activated;
  }

  // token을 통해 userTemp.user -> user.email 가져옴
  public String fetchEmailByToken() {
    return this.email;
  }

  // 이메일 인증으로 유저 활성화
  public void activate() {
    this.activated = true;
  }

  // 사용자정보 fetch
  public ProfileResponseDto buildProfileResponseDto(String imagePathAvatar) {
    String avatarUrl = (this.avatarImageSmall != null)
        ? imagePathAvatar + Paths.get(this.avatarImageSmall).getFileName().toString()
        : null;

    return ProfileResponseDto.builder()
        .id(this.id)
        .email(this.email)
        .username(this.username)
        .avatar(avatarUrl)
        .build();
  }

  // 비밀번호 복구 재설정 userTemp.user -> user.password
  public void recoverPassword(String password) {
    this.password = password;
  }

  // 비밀번호 재설정시 이전 비밀번호 확인
  public boolean checkPassword(String password, PasswordEncoder passwordEncoder) {
    return passwordEncoder.matches(password, this.password);
  }

  // 사용자명 재설정
  public void changeUsername(String username) {
    this.username = username;
  }

  // 아바타 재설정
  public void changeAvatar(String avatarImageSmall, String avatarImageLarge) {
    this.avatarImageSmall = avatarImageSmall;
    this.avatarImageLarge = avatarImageLarge;
  }

  public Map<String, String> fetchAvatarPathMapForDeleteChangeAvatar() {
    Map<String, String> avatarPathMap = new HashMap<>();

    avatarPathMap.put("small", this.avatarImageSmall);
    avatarPathMap.put("large", this.avatarImageLarge);
    return avatarPathMap;
  }

  // 비밀번호 재설정
  public void changePassword(String password) {
    this.password = password;
  }

  // 유저 삭제
  public void logicDelete() {
    this.logicDelete = true;
  }

  public UserInfoForServerJoinResponseDto fetchUserInfoForServerJoinResponse() {
    return UserInfoForServerJoinResponseDto.builder()
        .id(this.id)
        .username(this.username)
        .build();
  }


  public Long fetchUserIdForFriendRequest() {
    return this.id;
  }

  public Long fetchUserIdForServerLeaveResponse() {
    return this.id;
  }

  public Long fetchUserIdForCreateToken() {
    return this.id;
  }
}