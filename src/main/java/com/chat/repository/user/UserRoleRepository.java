package com.chat.repository.user;

import com.chat.domain.user.User;
import com.chat.domain.user.UserRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

  List<UserRole> findByUser(User user);
}
