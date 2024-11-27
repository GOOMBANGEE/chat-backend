package com.chat.repository.user;

import com.chat.domain.channel.Channel;
import com.chat.domain.user.Notification;
import com.chat.domain.user.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  List<Notification> findByChannelAndMentionedUserAndIsReadFalse(Channel channel, User user);

}
