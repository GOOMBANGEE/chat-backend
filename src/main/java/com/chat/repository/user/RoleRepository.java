package com.chat.repository.user;


import com.chat.domain.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> {

}
