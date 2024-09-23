package com.chat.repository.channel;

import com.chat.domain.channel.QChannel;
import com.chat.domain.server.Server;
import com.chat.dto.MessageQueueInitializeDto;
import com.chat.dto.QMessageQueueInitializeDto;
import com.chat.dto.channel.ChannelInfoDto;
import com.chat.dto.channel.QChannelInfoDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChannelRepositoryImpl implements ChannelRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  QChannel qChannel = QChannel.channel;

  @Override
  public List<ChannelInfoDto> fetchChannelInfoDtoListByServerIdList(List<Long> serverIdList) {
    return queryFactory
        .select(new QChannelInfoDto(
            qChannel.id,
            qChannel.name,
            qChannel.displayOrder,
            qChannel.server.id,
            qChannel.category.id))
        .from(qChannel)
        .where(serverIdIn(serverIdList), logicDeleteFalse())
        .fetch();
  }

  private BooleanExpression serverIdIn(List<Long> serverIdList) {
    return qChannel.server.id.in(serverIdList);
  }

  private BooleanExpression logicDeleteFalse() {
    return qChannel.logicDelete.isFalse();
  }

  @Override
  public List<MessageQueueInitializeDto> fetchMessageQueueInitializeDtoList() {
    return queryFactory
        .select(new QMessageQueueInitializeDto(
            qChannel.server.id,
            qChannel.id))
        .from(qChannel)
        .where(logicDeleteFalse())
        .fetch();
  }
}
