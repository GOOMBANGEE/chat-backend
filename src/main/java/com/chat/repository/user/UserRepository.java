package com.chat.repository.user;


import com.chat.domain.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmailAndLogicDeleteFalse(String email);

  Optional<User> findByUsernameAndLogicDeleteFalse(String username);
}
