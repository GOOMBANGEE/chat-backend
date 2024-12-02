package com.chat.repository.channel;

import com.chat.domain.channel.ChannelServerRoleRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelServerRoleRelationRepository extends
    JpaRepository<ChannelServerRoleRelation, Long> {

  @Modifying
  @Query("DELETE ChannelServerRoleRelation csrr WHERE csrr.channel.id =:channelId")
  void bulkDeleteByChannelId(Long channelId);
}
