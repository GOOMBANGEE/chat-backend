package com.chat.repository.user;


import com.chat.domain.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByIdAndLogicDeleteFalse(Long id);

  Optional<User> findByEmailAndLogicDeleteFalse(String email);

  Optional<User> findByUsernameAndLogicDeleteFalse(String username);

  List<User> findByIdInAndLogicDeleteFalse(List<Long> idList);

  @Modifying
  @Query("UPDATE User u SET u.online = false WHERE u.id IN :userIds")
  void bulkUpdateOffline(@Param("userIds") List<Long> userIds);

}
