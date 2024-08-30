package com.chat.repository.server;

import com.chat.domain.server.Server;
import com.chat.domain.server.ServerUserRelation;
import com.chat.domain.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerUserRelationRepository extends
    JpaRepository<ServerUserRelation, Long>,
    ServerUserRelationRepositoryCustom {


  Optional<ServerUserRelation> findServerUserRelationByUserAndServer(User user, Server server);

  Optional<ServerUserRelation> findByServerAndOwnerFalseAndLogicDeleteFalse(Server server);
}
