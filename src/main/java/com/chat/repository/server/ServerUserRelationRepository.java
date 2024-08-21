package com.chat.repository.server;

import com.chat.domain.server.ServerUserRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerUserRelationRepository extends
    JpaRepository<ServerUserRelation, Long>,
    ServerUserRelationRepositoryCustom {

}
