package com.chat.repository.chat;

import static org.springframework.util.ObjectUtils.isEmpty;

import com.chat.domain.channel.Channel;
import com.chat.domain.chat.QChat;
import com.chat.domain.user.QUser;
import com.chat.dto.chat.ChatInfoDto;
import com.chat.dto.chat.ChatReferenceInfoForSendMessageResponse;
import com.chat.dto.chat.QChatInfoDto;
import com.chat.dto.chat.QChatReferenceInfoForSendMessageResponse;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatQueryRepository {

  private final JPAQueryFactory queryFactory;

  QChat qChat = QChat.chat;
  QChat qChatReference = new QChat("qChatReference");
  QUser qUser = QUser.user;

  private QChatInfoDto chatInfoDtoProjection() {
    return new QChatInfoDto(
        qChat.id,
        qChat.user.id,
        qChat.user.username,
        qChat.user.avatarImageSmall,
        qChat.message,
        qChat.attachmentType,
        qChat.attachment,
        qChat.attachmentWidth,
        qChat.attachmentHeight,
        qChat.enter,
        qChat.createTime,
        qChat.updateTime,
        qChatReference.id,
        qChatReference.user.username,
        qChatReference.user.avatarImageSmall,
        qChatReference.message,
        qChatReference.attachmentType
    );
  }

  public List<ChatInfoDto> fetchChatInfoDtoListByChannelId(Long channelId) {
    return queryFactory
        .select(chatInfoDtoProjection())
        .from(qChat)
        .where(channelIdEq(channelId), chatDeleteFalse())
        .leftJoin(qChat.chatReference, qChatReference)
        .leftJoin(qChatReference.user, qUser)
        .orderBy(qChat.id.desc())
        .limit(50)
        .fetch();
  }

  private BooleanExpression channelIdEq(Long channelId) {
    return isEmpty(channelId) ? null : qChat.channel.id.eq(channelId);
  }

  private BooleanExpression chatDeleteFalse() {
    return qChat.logicDelete.eq(Boolean.FALSE);
  }

  public List<ChatInfoDto> fetchChatInfoDtoListByChannelIdAndChatId(Long channelId, Long chatId) {
    return queryFactory
        .select(chatInfoDtoProjection())
        .from(qChat)
        .where(channelIdEq(channelId), chatDeleteFalse(), chatIdLt(chatId))
        .leftJoin(qChat.chatReference, qChatReference)
        .leftJoin(qChatReference.user, qUser)
        .orderBy(qChat.id.desc())
        .limit(50)
        .fetch();
  }

  private BooleanExpression chatIdLt(Long chatId) {
    return isEmpty(chatId) ? null : qChat.id.lt(chatId);
  }

  public Page<ChatInfoDto> searchChatInfoDtoListDefault(Channel channel, String keyword,
      Pageable pageable) {
    List<ChatInfoDto> content = queryFactory
        .select(chatInfoDtoProjection())
        .from(qChat)
        .where(channelEq(channel),
            searchChatInfoDtoListDefaultOption(keyword),
            chatDeleteFalse(),
            enterFalse())
        .leftJoin(qChat.chatReference, qChatReference)
        .leftJoin(qChatReference.user, qUser)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(qChat.id.desc())
        .fetch();

    JPAQuery<ChatInfoDto> countQuery = queryFactory
        .select(chatInfoDtoProjection())
        .from(qChat)
        .where(channelEq(channel),
            searchChatInfoDtoListDefaultOption(keyword),
            chatDeleteFalse(),
            enterFalse())
        .leftJoin(qChat.chatReference, qChatReference)
        .leftJoin(qChatReference.user, qUser);

    return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetch().size());
  }

  private BooleanExpression searchChatInfoDtoListDefaultOption(String keyword) {
    return qChat.user.username.like("%" + keyword + "%")
        .or(qChat.message.like("%" + keyword + "%"));
  }

  private BooleanExpression enterFalse() {
    return qChat.enter.isFalse();
  }

  public Page<ChatInfoDto> searchChatInfoDtoListByUsername(Channel channel, String username,
      Pageable pageable) {
    List<ChatInfoDto> content = queryFactory
        .select(chatInfoDtoProjection())
        .from(qChat)
        .where(channelEq(channel),
            searchChatInfoDtoListByUsernameOption(username),
            chatDeleteFalse(),
            enterFalse())
        .leftJoin(qChat.chatReference, qChatReference)
        .leftJoin(qChatReference.user, qUser)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(qChat.id.desc())
        .fetch();

    JPAQuery<ChatInfoDto> countQuery = queryFactory
        .select(chatInfoDtoProjection())
        .from(qChat)
        .where(channelEq(channel),
            searchChatInfoDtoListDefaultOption(username),
            chatDeleteFalse(),
            enterFalse())
        .leftJoin(qChat.chatReference, qChatReference)
        .leftJoin(qChatReference.user, qUser);

    return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetch().size());
  }

  private BooleanExpression searchChatInfoDtoListByUsernameOption(String username) {
    return qChat.user.username.like("%" + username + "%");
  }

  public Page<ChatInfoDto> searchChatInfoDtoListByMessage(Channel channel, String message,
      Pageable pageable) {
    List<ChatInfoDto> content = queryFactory
        .select(chatInfoDtoProjection())
        .from(qChat)
        .where(channelEq(channel),
            searchChatInfoDtoListByMessageOption(message),
            chatDeleteFalse(),
            enterFalse())
        .leftJoin(qChat.chatReference, qChatReference)
        .leftJoin(qChatReference.user, qUser)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(qChat.id.desc())
        .fetch();

    JPAQuery<ChatInfoDto> countQuery = queryFactory
        .select(chatInfoDtoProjection())
        .from(qChat)
        .where(channelEq(channel),
            searchChatInfoDtoListDefaultOption(message),
            chatDeleteFalse(),
            enterFalse())
        .leftJoin(qChat.chatReference, qChatReference)
        .leftJoin(qChatReference.user, qUser);

    return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetch().size());
  }

  private BooleanExpression searchChatInfoDtoListByMessageOption(String message) {
    return qChat.message.like("%" + message + "%");
  }

  public Page<ChatInfoDto> searchChatInfoDtoListByUsernameAndMessage(Channel channel,
      String username,
      String message, Pageable pageable) {
    List<ChatInfoDto> content = queryFactory
        .select(chatInfoDtoProjection())
        .from(qChat)
        .where(channelEq(channel),
            searchChatInfoDtoListByUsernameAndMessageOption(username, message),
            chatDeleteFalse(),
            enterFalse())
        .leftJoin(qChat.chatReference, qChatReference)
        .leftJoin(qChatReference.user, qUser)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(qChat.id.desc())
        .fetch();

    JPAQuery<ChatInfoDto> countQuery = queryFactory
        .select(chatInfoDtoProjection())
        .from(qChat)
        .where(channelEq(channel),
            searchChatInfoDtoListDefaultOption(username),
            chatDeleteFalse(),
            enterFalse())
        .leftJoin(qChat.chatReference, qChatReference)
        .leftJoin(qChatReference.user, qUser);

    return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetch().size());
  }

  private BooleanExpression searchChatInfoDtoListByUsernameAndMessageOption(String username,
      String message) {
    return qChat.user.username.like("%" + username + "%")
        .and(qChat.message.like("%" + message + "%"));
  }

  public ChatReferenceInfoForSendMessageResponse fetchChatReferenceInfoForSendMessageResponseByChatIdAndChannel(
      Long chatId, Channel channel) {
    return queryFactory
        .select(new QChatReferenceInfoForSendMessageResponse(
            qChat,
            qChat.user,
            qChat.id,
            qChat.user.username,
            qChat.user.avatarImageSmall,
            qChat.message,
            qChat.attachmentType))
        .from(qChat)
        .where(chatIdEq(chatId), channelEq(channel), chatDeleteFalse())
        .fetchFirst();
  }

  private BooleanExpression chatIdEq(Long chatId) {
    return qChat.id.eq(chatId);
  }

  private BooleanExpression channelEq(Channel channel) {
    return isEmpty(channel) ? null : qChat.channel.eq(channel);
  }
}
