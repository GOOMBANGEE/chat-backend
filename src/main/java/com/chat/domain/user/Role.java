package com.chat.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role {

  @Id
  @Column(name = "role_name", length = 50)
  private String roleName;

  @Builder
  public Role(String roleName) {
    this.roleName = roleName;
  }

  public SimpleGrantedAuthority createGrantedAuthorities() {
    return new SimpleGrantedAuthority(this.roleName);
  }
}