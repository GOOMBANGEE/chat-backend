package com.chat.domain.category;

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
public class Category {

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

  @Builder
  public Category(String name, Double displayOrder, boolean open, boolean logicDelete,
      Server server) {
    this.name = name;
    this.displayOrder = displayOrder;
    this.open = open;
    this.logicDelete = logicDelete;
    this.server = server;
  }

  public Long getCategoryIdForServerCreateResponse() {
    return this.id;
  }
}
