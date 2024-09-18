package com.chat.repository.server;

import com.chat.domain.server.ServerRoleUserRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerRoleUserRelationRepository extends
    JpaRepository<ServerRoleUserRelation, Long>, ServerRoleUserRelationRepositoryCustom {

}
