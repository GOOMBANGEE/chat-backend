package com.chat.domain.channel;

import com.chat.domain.category.Category;
import com.chat.domain.server.Server;
import com.chat.dto.channel.ChannelSettingRequestDto;
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
public class Channel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  private Double displayOrder;

  private boolean open;

  private boolean logicDelete;

  private Long lastMessageId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private Server server;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private Category category;

  @Builder
  public Channel(String name, Double displayOrder, boolean open, boolean logicDelete, Server server,
      Category category) {
    this.name = name;
    this.displayOrder = displayOrder;
    this.open = open;
    this.logicDelete = logicDelete;
    this.server = server;
    this.category = category;
  }

  public Long getChannelIdForServerCreate() {
    return this.id;
  }

  public Long getChannelIdForChannelCreate() {
    return this.id;
  }

  public boolean isOpen() {
    return open;
  }

  public void rename(String name) {
    this.name = name;
  }

  // channel setting
  public void setting(ChannelSettingRequestDto requestDto, Category category) {
    this.name = requestDto.getName();
    this.displayOrder = requestDto.getDisplayOrder();
    this.open = requestDto.isOpen();
    this.category = category;
  }

  // channel delete
  public void logicDelete() {
    this.logicDelete = true;
  }

  public void updateLastMessageId(Long chatId) {
    this.lastMessageId = chatId;
  }

  public void deleteCategory(Double displayOrder) {
    this.displayOrder = displayOrder;
    this.category = null;
  }
}
