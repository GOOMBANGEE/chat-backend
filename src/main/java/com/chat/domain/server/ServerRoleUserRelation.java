package com.chat.domain.server;

import com.chat.domain.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerRoleUserRelation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private ServerRole serverRole;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private User user;

  @Builder
  public ServerRoleUserRelation(ServerRole serverRole, User user) {
    this.serverRole = serverRole;
    this.user = user;
  }
}
