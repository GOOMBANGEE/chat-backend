package com.chat.repository.user;

import com.chat.domain.user.User;
import com.chat.domain.user.UserTemp;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTempRepository extends JpaRepository<UserTemp, Long> {

  Optional<UserTemp> findByUser(User user);

  Optional<UserTemp> findByToken(String token);
}
