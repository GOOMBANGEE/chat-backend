package com.chat.repository.user;

import com.chat.domain.user.UserFriendTemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFriendTempRepository extends JpaRepository<UserFriendTemp, Long> {

}
