package petcare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import petcare.dto.UserDTO;
import petcare.mapper.UserMapper;

// Spring Security에서 사용자 정보를 로드하는 서비스.
@Service
@RequiredArgsConstructor
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        UserDTO user = userMapper.findByUserId(userId);
        if (user == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId);
        }
        
        String role = user.getRole() != null ? user.getRole() : "USER";
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUserId())
                .password(user.getPassword())
                .roles(role)
                .build();
    }
}