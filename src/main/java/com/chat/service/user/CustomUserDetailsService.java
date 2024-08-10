package com.chat.service.user;

import com.chat.domain.user.User;
import com.chat.domain.user.UserRole;
import com.chat.repository.user.UserRepository;
import com.chat.repository.user.UserRoleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;
  private final UserRoleRepository userRoleRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return userRepository.findByEmail(email)
        .map(this::createUserDetailsUser)
        .orElseThrow(() -> new UsernameNotFoundException(email + " -> 데이터베이스에서 찾을 수 없습니다."));
  }

  // DB 의 User 값으로 UserDetails 객체 생성 후 반환
  private org.springframework.security.core.userdetails.User createUserDetailsUser(User user) {
    List<UserRole> userRoles = userRoleRepository.findByUser(user);

    List<SimpleGrantedAuthority> grantedAuthorities = userRoles.stream()
        .map(UserRole::createGrantedAuthorities).toList();

    return user.buildUserDetails(grantedAuthorities);
  }
}
