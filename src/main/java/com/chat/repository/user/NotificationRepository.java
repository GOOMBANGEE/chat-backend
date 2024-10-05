package com.chat.repository.user;

import com.chat.domain.chat.Chat;
import com.chat.domain.user.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  List<Notification> findByChat(Chat chat);
}
