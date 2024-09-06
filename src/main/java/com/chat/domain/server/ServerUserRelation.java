package com.chat.domain.server;

import com.chat.domain.user.User;
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

@Entity
@Table(name = "server_user_relation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerUserRelation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "server", referencedColumnName = "id")
  private Server server;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user", referencedColumnName = "id")
  private User user;

  private boolean owner;

  private boolean logicDelete;

  @Builder
  public ServerUserRelation(Server server, User user, boolean owner) {
    this.server = server;
    this.user = user;
    this.owner = owner;
  }

  public boolean isOwner() {
    return owner;
  }

  public void logicDelete() {
    this.logicDelete = true;
  }

  public boolean isLogicDelete() {
    return logicDelete;
  }

  public void reJoin() {
    this.logicDelete = false;
  }

}
