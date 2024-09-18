package com.chat.repository.server;

import com.chat.domain.server.ServerRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerRoleRepository extends JpaRepository<ServerRole, Long> {

  List<ServerRole> findByIdInAndLogicDeleteFalse(List<Long> idList);
}
