package com.chat.repository.channel;

import com.chat.domain.channel.Channel;
import com.chat.domain.channel.ChannelUserRelation;
import com.chat.domain.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelUserRelationRepository extends JpaRepository<ChannelUserRelation, Long>,
    ChannelUserRelationRepositoryCustom {

  Optional<ChannelUserRelation> findByChannelAndUser(Channel channel, User user);
}
