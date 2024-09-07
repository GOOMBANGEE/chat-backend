package com.chat.domain.server;

import com.chat.dto.server.ServerInviteInfoResponseDto;
import com.chat.dto.server.ServerJoinResponseDto;
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

  private boolean logicDelete;

  @Builder
  public Server(String name, String ownerUsername, Long userCount, boolean logicDelete) {
    this.name = name;
    this.ownerUsername = ownerUsername;
    this.userCount = userCount;
    this.logicDelete = logicDelete;
  }

  public Long getServerIdForServerCreateResponse() {
    return this.id;
  }

  public void changeServerName(String newName) {
    this.name = newName;
  }

  public ServerJoinResponseDto getServerIdForServerJoinResponse() {
    return ServerJoinResponseDto.builder()
        .id(this.id)
        .name(this.name)
        .build();
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

  public void userJoin() {
    this.userCount += 1;
  }

  public Long userLeave() {
    this.userCount -= 1;
    return this.userCount;
  }

  public boolean checkName(String name) {
    return this.name.equals(name);
  }

  public void logicDelete() {
    this.logicDelete = true;
  }
}
