package com.chat.domain.user;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_temp_reset")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTempReset {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String token;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  private User user;

  @Builder
  public UserTempReset(String token, User user) {
    this.token = token;
    this.user = user;
  }

  // token을 통해 userTemp -> user.email 가져옴
  public String fetchEmailByToken() {
    return this.user.fetchEmailByToken();
  }

  // 비밀번호 분실시 발송했던 이메일을 재발송
  public String tokenForResend() {
    return this.token;
  }

  // 비밀번호 복구 재설정 userTemp.user -> user.password
  public void recoverPassword(String password) {
    this.user.recoverPassword(password);
  }
}
