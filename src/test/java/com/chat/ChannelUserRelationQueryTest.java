package com.chat;

import com.chat.domain.category.Category;
import com.chat.domain.channel.Channel;
import com.chat.domain.channel.ChannelUserRelation;
import com.chat.domain.server.Server;
import com.chat.domain.server.ServerUserRelation;
import com.chat.domain.user.User;
import com.chat.repository.category.CategoryRepository;
import com.chat.repository.channel.ChannelRepository;
import com.chat.repository.channel.ChannelUserRelationQueryRepository;
import com.chat.repository.channel.ChannelUserRelationRepository;
import com.chat.repository.server.ServerRepository;
import com.chat.repository.server.ServerUserRelationQueryRepository;
import com.chat.repository.server.ServerUserRelationRepository;
import com.chat.repository.user.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@DataJpaTest
@ComponentScan(basePackages = "com.chat.repository")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChannelUserRelationQueryTest {

  @Autowired
  private EntityManager em;
  @Autowired
  private ServerRepository serverRepository;
  @Autowired
  private ServerUserRelationRepository serverUserRelationRepository;
  @Autowired
  private ServerUserRelationQueryRepository serverUserRelationQueryRepository;
  @Autowired
  private CategoryRepository categoryRepository;
  @Autowired
  private ChannelRepository channelRepository;
  @Autowired
  private ChannelUserRelationRepository channelUserRelationRepository;
  @Autowired
  private ChannelUserRelationQueryRepository channelUserRelationQueryRepository;
  @Autowired
  private UserRepository userRepository;

  private Channel channel;
  private Long channelId;
  private List<User> userList = new ArrayList<>();

  private Server server;
  private long startTime;

  @BeforeEach
  void setUp() {
    server = Server.builder()
        .name("name")
        .icon("iconFilePath")
        .ownerUsername("username")
        .userCount(1L)
        .logicDelete(false)
        .build();
    serverRepository.save(server);

    Category category = Category.builder()
        .name("name")
        .displayOrder(1.0)
        .open(true)
        .logicDelete(false)
        .server(server)
        .build();
    categoryRepository.save(category);

    channel = Channel.builder()
        .name("name")
        .displayOrder(1.0)
        .open(true)
        .server(server)
        .category(category)
        .build();
    channelRepository.save(channel);
    channelId = channel.getChannelIdForChannelCreate();

    userList = LongStream.range(1, 1001)
        .mapToObj(id -> User.builder()
            .email("user" + id + "@email.com")
            .username("user" + id)
            .password("user" + id)
            .registerDate(LocalDateTime.now())
            .activated(true)
            .logicDelete(false)
            .build())
        .toList();
    userRepository.saveAll(userList);

    List<ServerUserRelation> serverUserRelationList = new ArrayList<>();
    userList.forEach(user -> {
      serverUserRelationList.add(
          ServerUserRelation.builder()
              .server(server)
              .user(user)
              .owner(false)
              .build());
    });
    serverUserRelationRepository.saveAll(serverUserRelationList);
    em.flush();
  }

  @AfterEach
  void after() {
    long endTime = System.nanoTime(); // 종료 시간 기록
    long duration = endTime - startTime;
    System.out.printf("Test completed in %d ms%n", duration / 1_000_000);

    channelUserRelationRepository.deleteAll();
  }

  @Test
  void bulkInsert_test() {
    List<Long> userIdList = serverUserRelationQueryRepository.fetchUserIdListByServer(server);

    startTime = System.nanoTime(); // 시작 시간 기록
    channelUserRelationQueryRepository.bulkInsertChannelCreate(channelId, userIdList);
    em.flush();
  }

  @Test
  void saveAll_test() {
    List<ChannelUserRelation> channelUserRelationList = new ArrayList<>();
    userList.forEach(
        serverUser -> {
          ChannelUserRelation channelUserRelation = ChannelUserRelation.builder()
              .channel(channel)
              .user(serverUser)
              .readMessage(true)
              .writeMessage(true)
              .viewHistory(true)
              .build();
          channelUserRelationList.add(channelUserRelation);
        }
    );

    startTime = System.nanoTime(); // 시작 시간 기록
    channelUserRelationRepository.saveAll(channelUserRelationList);
    em.flush();
  }

  @Test
  void bulkUpdate_test() {
    List<ChannelUserRelation> channelUserRelationList = new ArrayList<>();
    userList.forEach(
        serverUser -> {
          ChannelUserRelation channelUserRelation = ChannelUserRelation.builder()
              .channel(channel)
              .user(serverUser)
              .readMessage(true)
              .writeMessage(true)
              .viewHistory(true)
              .build();
          channelUserRelationList.add(channelUserRelation);
        }
    );
    channelUserRelationList.forEach(ChannelUserRelation::subscribe);
    channelUserRelationRepository.saveAll(channelUserRelationList);
    em.flush();

    startTime = System.nanoTime(); // 시작 시간 기록
    channelUserRelationQueryRepository.bulkUpdateUnsubscribe(channelUserRelationList);
    em.flush();
  }

  @Test
  void updateAll_test() {
    List<ChannelUserRelation> channelUserRelationList = new ArrayList<>();
    userList.forEach(
        serverUser -> {
          ChannelUserRelation channelUserRelation = ChannelUserRelation.builder()
              .channel(channel)
              .user(serverUser)
              .readMessage(true)
              .writeMessage(true)
              .viewHistory(true)
              .build();
          channelUserRelationList.add(channelUserRelation);
        }
    );
    channelUserRelationList.forEach(ChannelUserRelation::subscribe);
    channelUserRelationRepository.saveAll(channelUserRelationList);
    em.flush();

    startTime = System.nanoTime(); // 시작 시간 기록
    channelUserRelationList.forEach(ChannelUserRelation::unsubscribe);
    channelUserRelationRepository.saveAll(channelUserRelationList);
    em.flush();
  }


  @Test
  void bulkDelete_test() {
    List<Long> userIdList = serverUserRelationQueryRepository.fetchUserIdListByServer(server);
    channelUserRelationQueryRepository.bulkInsertChannelCreate(channelId, userIdList);

    startTime = System.nanoTime(); // 시작 시간 기록
    channelUserRelationQueryRepository.bulkDeleteByChannelId(channelId);
    em.flush();
  }

  @Test
  void deleteAll_test() {
    List<ChannelUserRelation> channelUserRelationList = new ArrayList<>();
    userList.forEach(
        serverUser -> {
          ChannelUserRelation channelUserRelation = ChannelUserRelation.builder()
              .channel(channel)
              .user(serverUser)
              .readMessage(true)
              .writeMessage(true)
              .viewHistory(true)
              .build();
          channelUserRelationList.add(channelUserRelation);
        }
    );
    channelUserRelationRepository.saveAll(channelUserRelationList);

    startTime = System.nanoTime(); // 시작 시간 기록
    channelUserRelationRepository.deleteAll(channelUserRelationList);
    em.flush();
  }
}
