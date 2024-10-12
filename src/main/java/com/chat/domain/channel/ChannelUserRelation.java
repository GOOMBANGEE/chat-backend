package com.chat.domain.channel;

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
public class ChannelUserRelation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private Channel channel;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private User user;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private User userDirectMessage;

  private boolean readMessage;

  private boolean writeMessage;

  private boolean viewHistory;

  private Long lastReadMessageId;

  @Builder
  public ChannelUserRelation(Long id, Channel channel, User user, User userDirectMessage,
      boolean readMessage, boolean writeMessage, boolean viewHistory, Long lastReadMessageId
  ) {
    this.id = id;
    this.channel = channel;
    this.user = user;
    this.userDirectMessage = userDirectMessage;
    this.readMessage = readMessage;
    this.writeMessage = writeMessage;
    this.viewHistory = viewHistory;
    this.lastReadMessageId = lastReadMessageId;
  }

  public void updateLastReadMessageId(Long lastReadMessageId) {
    this.lastReadMessageId = lastReadMessageId;
  }
}
