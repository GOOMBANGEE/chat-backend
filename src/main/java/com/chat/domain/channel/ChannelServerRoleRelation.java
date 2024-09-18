package com.chat.domain.channel;

import com.chat.domain.server.ServerRole;
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
public class ChannelServerRoleRelation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private Channel channel;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private ServerRole serverRole;

  private boolean readMessage;

  private boolean writeMessage;

  private boolean viewHistory;

  @Builder
  public ChannelServerRoleRelation(Channel channel, ServerRole serverRole, boolean readMessage,
      boolean writeMessage, boolean viewHistory) {
    this.channel = channel;
    this.serverRole = serverRole;
    this.readMessage = readMessage;
    this.writeMessage = writeMessage;
    this.viewHistory = viewHistory;
  }
}
