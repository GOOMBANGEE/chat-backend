package com.chat.service.category;

import com.chat.domain.category.Category;
import com.chat.domain.server.Server;
import com.chat.domain.server.ServerRole;
import com.chat.domain.server.ServerUserRelation;
import com.chat.domain.user.User;
import com.chat.dto.MessageDto;
import com.chat.dto.MessageDto.MessageType;
import com.chat.dto.category.CategoryCreateRequest;
import com.chat.dto.category.CategoryCreateResponseDto;
import com.chat.exception.CategoryException;
import com.chat.exception.ServerException;
import com.chat.exception.UserException;
import com.chat.repository.category.CategoryQueryRepository;
import com.chat.repository.category.CategoryRepository;
import com.chat.repository.category.CategoryServerRoleRelationRepository;
import com.chat.repository.category.CategoryUserRelationQueryRepository;
import com.chat.repository.channel.ChannelQueryRepository;
import com.chat.repository.server.ServerRepository;
import com.chat.repository.server.ServerRoleUserRelationQueryRepository;
import com.chat.repository.server.ServerUserRelationQueryRepository;
import com.chat.repository.server.ServerUserRelationRepository;
import com.chat.repository.user.UserRepository;
import com.chat.service.user.CustomUserDetailsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {

  private final CustomUserDetailsService customUserDetailsService;
  private final UserRepository userRepository;
  private final ServerRepository serverRepository;
  private final ServerRoleUserRelationQueryRepository serverRoleUserRelationQueryRepository;
  private final ServerUserRelationRepository serverUserRelationRepository;
  private final ServerUserRelationQueryRepository serverUserRelationQueryRepository;
  private final CategoryRepository categoryRepository;
  private final CategoryQueryRepository categoryQueryRepository;
  private final CategoryUserRelationQueryRepository categoryUserRelationQueryRepository;
  private final CategoryServerRoleRelationRepository categoryServerRoleRelationRepository;
  private final ChannelQueryRepository channelQueryRepository;

  private static final String USER_UNREGISTERED = "USER:USER_UNREGISTERED";
  private static final String SERVER_NOT_FOUND = "SERVER:SERVER_NOT_FOUND";
  private static final String CATEGORY_NOT_FOUND = "CATEGORY:CATEGORY_NOT_FOUND";
  private static final String SERVER_NOT_PARTICIPATED = "SERVER:SERVER_NOT_PARTICIPATED";
  private static final String NO_CATEGORY_CREATE_PERMISSION = "SERVER:NO_CATEGORY_CREATE_PERMISSION";

  private final SimpMessagingTemplate messagingTemplate;
  private static final String SUB_SERVER = "/sub/server/";
  private final ObjectMapper mapper = new ObjectMapper();


  @Transactional
  public void create(Long serverId, CategoryCreateRequest requestDto)
      throws JsonProcessingException {
    // 등록된 유저인지 확인
    String email = customUserDetailsService.getEmailByUserDetails();
    User user = userRepository
        .findByEmailAndLogicDeleteFalse(email)
        .orElseThrow(() -> new UserException(USER_UNREGISTERED));

    Server server = serverRepository
        .findByIdAndLogicDeleteFalse(serverId)
        .orElseThrow(() -> new ServerException(SERVER_NOT_FOUND));
    ServerUserRelation serverUserRelation = serverUserRelationRepository
        .findByUserAndServerAndLogicDeleteFalse(user, server)
        .orElseThrow(() -> new ServerException(SERVER_NOT_PARTICIPATED));
    List<ServerRole> serverRoleUserRelation = serverRoleUserRelationQueryRepository
        .fetchServerRoleListByServerAndUser(server, user);

    // 카테고리 생성 권한 확인
    // 서버의 주인이거나, 역할 중 하나가 채널 생성 권한이 있는지 확인
    boolean authorized = serverUserRelation.isOwner() ||
        serverRoleUserRelation
            .stream()
            .anyMatch(ServerRole::checkCreateChannel);

    // 권한이 없을 경우 예외처리
    if (!authorized) {
      throw new ServerException(NO_CATEGORY_CREATE_PERMISSION);
    }

    // 만약 역할, 유저를 지정한 경우 해당 역할, 유저에게 읽기 권한 부여
    List<Long> allowRoleIdList = requestDto.getAllowRoleIdList();
    List<Long> allowUserIdList = requestDto.getAllowUserIdList();

    String name = requestDto.getName();
    Double displayOrder = categoryQueryRepository.fetchMaxDisplayOrder(server) * 2;
    boolean open = allowRoleIdList == null && allowUserIdList == null;

    Category category = Category.builder()
        .name(name)
        .displayOrder(displayOrder)
        .open(open)
        .logicDelete(false)
        .server(server)
        .build();
    categoryRepository.save(category);
    Long categoryId = category.getCategoryIdForCategoryCreate();
    // 공개 채널인 경우 서버에 참가중인 모든 유저를 CategoryUserRelation에 추가
    if (open) {
      List<Long> userIdList = serverUserRelationQueryRepository.fetchUserIdListByServer(server);
      categoryUserRelationQueryRepository.bulkInsertCategoryIdAndUserIdList(categoryId, userIdList);
    }

    CategoryCreateResponseDto responseDto = CategoryCreateResponseDto.builder()
        .id(categoryId)
        .name(name)
        .displayOrder(displayOrder)
        .serverId(serverId)
        .allowRoleIdList(allowRoleIdList)
        .allowUserIdList(allowUserIdList)
        .build();
    // stomp pub
    String serverUrl = SUB_SERVER + serverId;
    MessageDto newMessageDto = MessageDto.builder()
        .messageType(MessageType.CATEGORY_CREATE)
        .serverId(serverId)
        .message(mapper.writeValueAsString(responseDto))
        .build();
    messagingTemplate.convertAndSend(serverUrl, newMessageDto);
  }


  @Transactional
  public void delete(Long serverId, Long categoryId) {
    Server server = serverRepository.findByIdAndLogicDeleteFalse(serverId)
        .orElseThrow(() -> new ServerException(SERVER_NOT_FOUND));

    Category category = categoryRepository
        .findByIdAndLogicDeleteFalseAndServerId(categoryId, serverId)
        .orElseThrow(() -> new CategoryException(CATEGORY_NOT_FOUND));

    category.logicDelete();
    categoryRepository.save(category);

    // Category에 속해있는 channel들에 대해 displayOrder 재설정 + category null 설정
    List<Long> channelIdList = channelQueryRepository
        .fetchChannelIdListByServerIdAndCategoryId(serverId, categoryId);
    final Double[] maxDisplayOrderCategoryNull = {
        channelQueryRepository.fetchMaxDisplayOrderByServerAndCategoryNull(server)};
    Map<Long, Double> map = new HashMap<>();
    channelIdList.forEach(id -> {
      map.put(id, maxDisplayOrderCategoryNull[0] * 2);
      maxDisplayOrderCategoryNull[0] = maxDisplayOrderCategoryNull[0] * 2;
    });
    channelQueryRepository.batchUpdateDisplayOrders(map);

    // CategoryUserRelation 모두 삭제
    categoryUserRelationQueryRepository.bulkDeleteByCategoryId(categoryId);

    // CategoryServerRoleRelation 모두 삭제
    categoryServerRoleRelationRepository.bulkDeleteByCategoryId(categoryId);

    // 카테고리 삭제 메시지 발송
    String serverUrl = SUB_SERVER + serverId;
    MessageDto newMessageDto = MessageDto.builder()
        .messageType(MessageType.CATEGORY_DELETE)
        .serverId(serverId)
        .categoryId(categoryId)
        .build();
    messagingTemplate.convertAndSend(serverUrl, newMessageDto);
  }
}
