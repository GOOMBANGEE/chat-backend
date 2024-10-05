package com.chat.repository.chat;

import static org.springframework.util.ObjectUtils.isEmpty;

import com.chat.domain.chat.QChat;
import com.chat.domain.server.QServer;
import com.chat.dto.chat.ChatInfoDto;
import com.chat.dto.chat.QChatInfoDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class ChatRepositoryImpl implements ChatRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  QServer qServer = QServer.server;
  QChat qChat = QChat.chat;

  @Override
  public List<ChatInfoDto> fetchChatInfoDtoListByChannelId(Long channelId) {
    return queryFactory
        .select(new QChatInfoDto(
            qChat.id,
            qChat.user.username,
            qChat.user.avatarImageSmall,
            qChat.message,
            qChat.attachmentType,
            qChat.attachment,
            qChat.enter,
            qChat.createTime,
            qChat.updateTime))
        .from(qChat)
        .where(channelIdEq(channelId), chatDeleteFalse())
        .orderBy(qChat.id.desc())
        .limit(50)
        .fetch();
  }

  private BooleanExpression channelIdEq(Long channelId) {
    return isEmpty(channelId) ? null : qChat.channel.id.eq(channelId);
  }

  private BooleanExpression serverIdEq(Long serverId) {
    return isEmpty(serverId) ? null : qServer.id.eq(serverId);
  }

  private BooleanExpression chatDeleteFalse() {
    return qChat.logicDelete.eq(Boolean.FALSE);
  }

  @Override
  public List<ChatInfoDto> fetchChatInfoDtoListByChannelIdAndChatId(Long channelId, Long chatId) {
    return queryFactory
        .select(new QChatInfoDto(
            qChat.id,
            qChat.user.username,
            qChat.user.avatarImageSmall,
            qChat.message,
            qChat.attachmentType,
            qChat.attachment,
            qChat.enter,
            qChat.createTime,
            qChat.updateTime))
        .from(qChat)
        .where(channelIdEq(channelId), chatDeleteFalse(), chatIdLt(chatId))
        .orderBy(qChat.id.desc())
        .limit(50)
        .fetch();
  }

  private BooleanExpression chatIdLt(Long chatId) {
    return isEmpty(chatId) ? null : qChat.id.lt(chatId);
  }

  @Override
  public Page<ChatInfoDto> searchChatInfoDtoListDefault(Long serverId, String keyword,
      Pageable pageable) {
    List<ChatInfoDto> content = queryFactory
        .select(new QChatInfoDto(
            qChat.id,
            qChat.user.username,
            qChat.user.avatarImageSmall,
            qChat.message,
            qChat.attachmentType,
            qChat.attachment,
            qChat.enter,
            qChat.createTime,
            qChat.updateTime))
        .from(qChat)
        .where(serverIdEq(serverId),
            searchChatInfoDtoListDefaultOption(keyword),
            chatDeleteFalse())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(qChat.id.desc())
        .fetch();

    JPAQuery<ChatInfoDto> countQuery = queryFactory
        .select(new QChatInfoDto(
            qChat.id,
            qChat.user.username,
            qChat.user.avatarImageSmall,
            qChat.message,
            qChat.attachmentType,
            qChat.attachment,
            qChat.enter,
            qChat.createTime,
            qChat.updateTime))
        .from(qChat)
        .where(serverIdEq(serverId),
            searchChatInfoDtoListDefaultOption(keyword),
            chatDeleteFalse());

    return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetch().size());
  }

  private BooleanExpression searchChatInfoDtoListDefaultOption(String keyword) {
    return qChat.user.username.like("%" + keyword + "%")
        .or(qChat.message.like("%" + keyword + "%"));
  }

  @Override
  public Page<ChatInfoDto> searchChatInfoDtoListByUsername(Long serverId, String username,
      Pageable pageable) {
    List<ChatInfoDto> content = queryFactory
        .select(new QChatInfoDto(
            qChat.id,
            qChat.user.username,
            qChat.user.avatarImageSmall,
            qChat.message,
            qChat.attachmentType,
            qChat.attachment,
            qChat.enter,
            qChat.createTime,
            qChat.updateTime))
        .from(qChat)
        .where(serverIdEq(serverId),
            searchChatInfoDtoListByUsernameOption(username),
            chatDeleteFalse())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(qChat.id.desc())
        .fetch();

    JPAQuery<ChatInfoDto> countQuery = queryFactory
        .select(new QChatInfoDto(
            qChat.id,
            qChat.user.username,
            qChat.user.avatarImageSmall,
            qChat.message,
            qChat.attachmentType,
            qChat.attachment,
            qChat.enter,
            qChat.createTime,
            qChat.updateTime))
        .from(qChat)
        .where(serverIdEq(serverId),
            searchChatInfoDtoListDefaultOption(username),
            chatDeleteFalse());

    return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetch().size());
  }

  private BooleanExpression searchChatInfoDtoListByUsernameOption(String username) {
    return qChat.user.username.like("%" + username + "%");
  }

  @Override
  public Page<ChatInfoDto> searchChatInfoDtoListByMessage(Long serverId, String message,
      Pageable pageable) {
    List<ChatInfoDto> content = queryFactory
        .select(new QChatInfoDto(
            qChat.id,
            qChat.user.username,
            qChat.user.avatarImageSmall,
            qChat.message,
            qChat.attachmentType,
            qChat.attachment,
            qChat.enter,
            qChat.createTime,
            qChat.updateTime))
        .from(qChat)
        .where(serverIdEq(serverId),
            searchChatInfoDtoListByMessageOption(message),
            chatDeleteFalse())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(qChat.id.desc())
        .fetch();

    JPAQuery<ChatInfoDto> countQuery = queryFactory
        .select(new QChatInfoDto(
            qChat.id,
            qChat.user.username,
            qChat.user.avatarImageSmall,
            qChat.message,
            qChat.attachmentType,
            qChat.attachment,
            qChat.enter,
            qChat.createTime,
            qChat.updateTime))
        .from(qChat)
        .where(serverIdEq(serverId),
            searchChatInfoDtoListDefaultOption(message),
            chatDeleteFalse());

    return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetch().size());
  }

  private BooleanExpression searchChatInfoDtoListByMessageOption(String message) {
    return qChat.message.like("%" + message + "%");
  }

  @Override
  public Page<ChatInfoDto> searchChatInfoDtoListByUsernameAndMessage(Long serverId, String username,
      String message, Pageable pageable) {
    List<ChatInfoDto> content = queryFactory
        .select(new QChatInfoDto(
            qChat.id,
            qChat.user.username,
            qChat.user.avatarImageSmall,
            qChat.message,
            qChat.attachmentType,
            qChat.attachment,
            qChat.enter,
            qChat.createTime,
            qChat.updateTime))
        .from(qChat)
        .where(serverIdEq(serverId),
            searchChatInfoDtoListByUsernameAndMessageOption(username, message),
            chatDeleteFalse())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(qChat.id.desc())
        .fetch();

    JPAQuery<ChatInfoDto> countQuery = queryFactory
        .select(new QChatInfoDto(
            qChat.id,
            qChat.user.username,
            qChat.user.avatarImageSmall,
            qChat.message,
            qChat.attachmentType,
            qChat.attachment,
            qChat.enter,
            qChat.createTime,
            qChat.updateTime))
        .from(qChat)
        .where(serverIdEq(serverId),
            searchChatInfoDtoListDefaultOption(username),
            chatDeleteFalse());

    return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetch().size());
  }

  private BooleanExpression searchChatInfoDtoListByUsernameAndMessageOption(String username,
      String message) {
    return qChat.user.username.like("%" + username + "%")
        .and(qChat.message.like("%" + message + "%"));
  }
}
