package com.chat.repository.server;

import com.chat.domain.server.Server;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerRepository extends JpaRepository<Server, Long> {

  Optional<Server> findByIdAndLogicDeleteFalse(Long id);

  Optional<Server> findByCodeAndLogicDeleteFalse(String code);
}
