package com.chat.repository.channel;

import com.chat.domain.channel.Channel;
import com.chat.domain.channel.ChannelServerRoleRelation;
import com.chat.domain.server.ServerRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelServerRoleRelationRepository extends
    JpaRepository<ChannelServerRoleRelation, Long>, ChannelServerRoleRelationRepositoryCustom {

  List<ChannelServerRoleRelation> findByChannelAndServerRoleIn(Channel channel,
      List<ServerRole> serverRoleList);
}
