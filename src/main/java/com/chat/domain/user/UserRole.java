package com.chat.domain.user;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Entity
@Table(name = "user_role")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRole {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  private User user;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "role_name", referencedColumnName = "role_name")
  private Role role;

  @Builder
  public UserRole(User user, Role role) {
    this.user = user;
    this.role = role;
  }

  public SimpleGrantedAuthority createGrantedAuthorities() {
    return this.role.createGrantedAuthorities();
  }
}
