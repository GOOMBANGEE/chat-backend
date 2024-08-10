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
@Table(name = "user_temp")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTemp {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String token;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  private User user;

  @Builder
  public UserTemp(String token, User user) {
    this.token = token;
    this.user = user;
  }

  // 이메일 재발송을 위한 토큰
  public String tokenForResend() {
    return token;
  }

  // token을 통해 userTemp.user -> user.email 가져옴
  public String fetchEmailByToken() {
    return this.user.fetchEmailByToken();
  }
}