package com.chat.domain.category;

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
public class CategoryUserRelation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private Category category;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private User user;

  private boolean readMessage;

  private boolean writeMessage;

  private boolean viewHistory;

  @Builder
  public CategoryUserRelation(Category category, User user, boolean readMessage,
      boolean writeMessage, boolean viewHistory) {
    this.category = category;
    this.user = user;
    this.readMessage = readMessage;
    this.writeMessage = writeMessage;
    this.viewHistory = viewHistory;
  }
}
