package com.chat.domain.server;

import com.chat.dto.server.ServerInviteInfoResponseDto;
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

  private String code;

  private String ownerUsername;

  private Long userCount;

  @Builder
  public Server(String name, String ownerUsername, Long userCount) {
    this.name = name;
    this.ownerUsername = ownerUsername;
    this.userCount = userCount;
  }

  public Long getServerIdForServerCreateResponse() {
    return this.id;
  }

  public Long getServerIdForServerJoinResponse() {
    return this.id;
  }

  public ServerInviteInfoResponseDto buildServerInviteInfoResponseDto() {
    return ServerInviteInfoResponseDto.builder()
        .name(this.name)
        .username(this.ownerUsername)
        .userCount(this.userCount)
        .build();
  }

  public boolean isPresentInviteCode() {
    return this.code != null;
  }

  public String fetchInviteCode() {
    return this.code;
  }

  public void createInviteCode(String code) {
    this.code = code;
  }
}
