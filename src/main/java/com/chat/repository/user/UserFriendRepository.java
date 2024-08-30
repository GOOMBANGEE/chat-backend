package com.chat.repository.user;

import com.chat.domain.user.UserFriend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFriendRepository extends JpaRepository<UserFriend, Long>,
    UserFriendRepositoryCustom {

}
