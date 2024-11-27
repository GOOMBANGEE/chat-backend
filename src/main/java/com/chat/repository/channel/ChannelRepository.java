package com.chat.repository.channel;

import com.chat.domain.category.Category;
import com.chat.domain.channel.Channel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {

  Optional<Channel> findByIdAndLogicDeleteFalseAndServerId(Long channelId, Long serverId);

  List<Channel> findByCategory(Category category);
}
