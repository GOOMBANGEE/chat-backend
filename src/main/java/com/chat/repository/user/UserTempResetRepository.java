package com.chat.repository.user;

import com.chat.domain.user.User;
import com.chat.domain.user.UserTempReset;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTempResetRepository extends JpaRepository<UserTempReset, Long> {

  Optional<UserTempReset> findByUser(User user);

  Optional<UserTempReset> findByToken(String token);
}
