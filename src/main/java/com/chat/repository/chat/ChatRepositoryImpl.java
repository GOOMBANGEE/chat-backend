package com.chat.repository.chat;

import static org.springframework.util.ObjectUtils.isEmpty;

import com.chat.domain.QChat;
import com.chat.domain.server.QServer;
import com.chat.dto.chat.ChatInfoDto;
import com.chat.dto.chat.QChatInfoDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChatRepositoryImpl implements ChatRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  QServer server = QServer.server;
  QChat chat = QChat.chat;

  @Override
  public List<ChatInfoDto> fetchChatInfoDtoListByServerId(Long serverId) {
    return queryFactory
        .select(new QChatInfoDto(chat.id, chat.user.username, chat.message))
        .from(chat)
        .where(serverIdEq(serverId))
        .orderBy(chat.id.desc())
        .limit(50)
        .fetch();
  }

  private BooleanExpression serverIdEq(Long serverId) {
    return isEmpty(serverId) ? null : server.id.eq(serverId);
  }
}
