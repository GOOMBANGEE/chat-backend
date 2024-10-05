package com.chat.repository.chat;

import com.chat.domain.channel.Channel;
import com.chat.domain.chat.Chat;
import com.chat.domain.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long>, ChatRepositoryCustom {

  Optional<Chat> findByIdAndUserAndLogicDeleteFalse(Long chatId, User user);

  Optional<Chat> findByIdAndChannelAndLogicDeleteFalse(Long chatId, Channel channel);
}
