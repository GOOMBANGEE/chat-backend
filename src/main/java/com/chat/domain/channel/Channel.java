package com.chat.domain.channel;

import com.chat.domain.category.Category;
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
public class Channel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  private Double displayOrder;

  private boolean open;

  private boolean logicDelete;

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

  public Long getChannelIdForServerCreateResponse() {
    return this.id;
  }

  public boolean isOpen() {
    return open;
  }
}
