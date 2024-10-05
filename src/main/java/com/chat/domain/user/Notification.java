package com.chat.domain.user;

import com.chat.domain.channel.Channel;
import com.chat.domain.chat.Chat;
import com.chat.domain.server.Server;
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
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private Server server;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private Channel channel;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private Chat chat;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private User user;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private User mentionedUser;

  private boolean read;

  @Builder
  public Notification(Server server, Channel channel, Chat chat, User user, User mentionedUser,
      boolean read) {
    this.server = server;
    this.channel = channel;
    this.chat = chat;
    this.user = user;
    this.mentionedUser = mentionedUser;
    this.read = read;
  }
}
