package com.chat.domain;

import com.chat.domain.server.Server;
import com.chat.domain.user.User;
import com.chat.dto.MessageDto;
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
@Table(name = "chat")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chat {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String message;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "server", referencedColumnName = "id")
  private Server server;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user", referencedColumnName = "id")
  private User user;

  private boolean logicDelete;

  @Builder
  public Chat(Long id, String message, Server server, User user, boolean logicDelete) {
    this.id = id;
    this.message = message;
    this.server = server;
    this.user = user;
    this.logicDelete = logicDelete;
  }

  public Long fetchChatIdForSendMessageResponse() {
    return this.id;
  }

  public MessageDto buildMessageDtoForSendMessageResponse(MessageDto messageDto) {
    return MessageDto.builder()
        .messageType(messageDto.getMessageType())
        .serverId(messageDto.getServerId())
        .chatId(this.id)
        .username(messageDto.getUsername())
        .message(this.message)
        .build();
  }

  public void updateMessage(MessageDto messageDto) {
    this.message = messageDto.getMessage();
  }

  public void logicDelete() {
    this.logicDelete = true;
  }
}
