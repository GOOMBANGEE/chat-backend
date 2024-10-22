package com.chat.domain.chat;

import com.chat.domain.channel.Channel;
import com.chat.domain.server.Server;
import com.chat.domain.user.User;
import com.chat.dto.MessageDto;
import com.chat.dto.MessageDto.MessageType;
import com.chat.dto.chat.ChatInfoDto;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chat {

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
  private User user;

  private String message;
  private String attachmentType;
  private String attachment;
  private Integer attachmentWidth;
  private Integer attachmentHeight;

  private boolean logicDelete = false;
  private boolean enter;

  private LocalDateTime createTime;
  private LocalDateTime updateTime;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private Chat chatReference;

  @Builder
  public Chat(
      Server server, Channel channel, User user,
      String message,
      String attachmentType, String attachment, Integer attachmentWidth, Integer attachmentHeight,
      boolean enter,
      LocalDateTime createTime, LocalDateTime updateTime) {
    this.server = server;
    this.channel = channel;
    this.user = user;
    this.message = message;
    this.attachmentType = attachmentType;
    this.attachment = attachment;
    this.attachmentWidth = attachmentWidth;
    this.attachmentHeight = attachmentHeight;
    this.enter = enter;
    this.createTime = createTime;
    this.updateTime = updateTime;
  }

  public Long fetchChatIdForSendMessageResponse() {
    return this.id;
  }

  public Long fetchChatIdForUpdateLastMessage() {
    return this.id;
  }

  public MessageDto buildMessageDtoForSendMessageResponse(MessageDto messageDto,
      Long userId, String avatar,
      ChatInfoDto chatRefInfoDto) {
    return MessageDto.builder()
        .messageType(messageDto.getMessageType())
        .serverId(messageDto.getServerId())
        .channelId(messageDto.getChannelId())
        .chatId(this.id)
        .userId(userId)
        .username(messageDto.getUsername())
        .avatar(avatar)
        .message(this.message)
        .attachmentType(this.attachmentType)
        .attachment(this.attachment)
        .attachmentWidth(this.attachmentWidth)
        .attachmentHeight(this.attachmentHeight)
        .chatReferenceInfo(chatRefInfoDto)
        .createTime(this.createTime)
        .build();
  }

  public void updateChatReference(Chat chatReference) {
    this.chatReference = chatReference;
  }

  public void updateMessage(MessageDto messageDto, LocalDateTime updateTime) {
    this.message = messageDto.getMessage();
    this.updateTime = updateTime;
  }

  public void logicDelete() {
    this.logicDelete = true;
  }

  public MessageDto buildMessageDtoForSeverJoinResponse(Long serverId, Long channelId, Long userId,
      String username) {
    return MessageDto.builder()
        .messageType(MessageType.SERVER_ENTER)
        .serverId(serverId)
        .channelId(channelId)
        .chatId(this.id)
        .userId(userId)
        .username(username)
        .createTime(this.createTime)
        .build();
  }
}
