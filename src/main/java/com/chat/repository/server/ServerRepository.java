package com.chat.repository.server;

import com.chat.domain.server.Server;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerRepository extends JpaRepository<Server, Long> {

}
