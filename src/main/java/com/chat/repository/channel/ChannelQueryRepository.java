package com.chat.repository.channel;

import static org.springframework.util.ObjectUtils.isEmpty;

import com.chat.domain.category.Category;
import com.chat.domain.channel.QChannel;
import com.chat.domain.server.Server;
import com.chat.dto.MessageQueueInitializeDto;
import com.chat.dto.QMessageQueueInitializeDto;
import com.chat.dto.channel.ChannelRegistrationDto;
import com.chat.dto.channel.QChannelRegistrationDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChannelQueryRepository {

  private final JPAQueryFactory queryFactory;
  QChannel qChannel = QChannel.channel;

  public List<ChannelRegistrationDto> fetchChannelRegistrationDtoListByServer(Server server) {
    return queryFactory
        .select(new QChannelRegistrationDto(
            qChannel,
            qChannel.lastMessageId
        ))
        .from(qChannel)
        .where(serverEq(server), openTrue(), logicDeleteFalse())
        .fetch();
  }

  private BooleanExpression logicDeleteFalse() {
    return qChannel.logicDelete.isFalse();
  }

  private BooleanExpression serverEq(Server server) {
    return isEmpty(server) ? null : qChannel.server.eq(server);
  }

  private BooleanExpression openTrue() {
    return qChannel.open.isTrue();
  }

  public List<MessageQueueInitializeDto> fetchMessageQueueInitializeDtoList() {
    return queryFactory
        .select(new QMessageQueueInitializeDto(
            qChannel.server.id,
            qChannel.id))
        .from(qChannel)
        .where(logicDeleteFalse())
        .fetch();
  }

  public Double fetchMaxDisplayOrderByCategory(Category category) {
    return queryFactory
        .select(qChannel.displayOrder.max().coalesce(1024.0))
        .from(qChannel)
        .where(categoryEq(category))
        .fetchFirst();
  }

  private BooleanExpression categoryEq(Category category) {
    return isEmpty(category) ? null : qChannel.category.eq(category);
  }

  public Double fetchMaxDisplayOrderByServerAndCategoryNull(Server server) {
    return queryFactory
        .select(qChannel.displayOrder.max().coalesce(1024.0))
        .from(qChannel)
        .where(serverEq(server), categoryNull())
        .fetchFirst();
  }

  private BooleanExpression categoryNull() {
    return qChannel.category.isNull();
  }
}
