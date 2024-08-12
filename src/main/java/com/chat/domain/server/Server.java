package com.chat.domain.server;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "server")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Server {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  private String description;

  // icon , banner , role, owner_id

  @Builder
  public Server(String name, String description) {
    this.name = name;
    this.description = description;
  }

  public Long getServerIdForServerCreateResponse() {
    return this.id;
  }
}
