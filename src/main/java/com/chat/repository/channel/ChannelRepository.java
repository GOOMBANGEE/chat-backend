package com.chat.repository.channel;

import com.chat.domain.channel.Channel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long>, ChannelRepositoryCustom {

  Optional<Channel> findByIdAndLogicDeleteFalseAndServerId(Long channelId, Long serverId);
  
}
