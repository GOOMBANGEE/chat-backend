package com.chat.repository.channel;

import com.chat.domain.channel.Channel;
import com.chat.domain.channel.ChannelServerRoleRelation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelServerRoleRelationRepository extends
    JpaRepository<ChannelServerRoleRelation, Long> {

  List<ChannelServerRoleRelation> findByChannel(Channel channel);
}
