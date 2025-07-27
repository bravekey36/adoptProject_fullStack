package petcare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import petcare.dto.UserDTO;
import petcare.mapper.UserMapper;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    // 전화번호만 단독 수정 (컨트롤러에서 사용)
    public boolean updateUserPhone(String userId, String phone) {
        UserDTO user = userMapper.findByUserId(userId);
        if (user == null) return false;
        user.setPhone(phone);
        userMapper.updateUserAdditionalInfo(user);
        return true;
    }
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserDTO findByUserId(String userId) {
        UserDTO user = userMapper.findByUserId(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        return user; // 전체 정보 반환
    }

    public boolean existsByUserId(String userId) {
        UserDTO user = userMapper.findByUserId(userId);
        return user != null;
    }

    public boolean existsByEmail(String email) {
        UserDTO user = userMapper.findByEmail(email);
        return user != null;
    }

    public UserDTO findByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    public void registerUser(UserDTO userDTO) {
        if (userDTO.getProvider() == null || "LOCAL".equals(userDTO.getProvider())) {
            // 일반 회원가입: provider를 명확히 LOCAL로 세팅
            userDTO.setProvider("LOCAL");
            if (userDTO.getPassword() == null || userDTO.getPassword().trim().isEmpty()) {
                throw new RuntimeException("비밀번호는 필수입니다.");
            }
            userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            if (userDTO.getRole() == null) {
                userDTO.setRole("USER");
            }
            userMapper.insertUser(userDTO);
        } else {
            // 소셜 로그인: user_id는 이미 세팅되어 있으므로 건드리지 않음
            userDTO.setPassword(null);
            if (userDTO.getRole() == null) {
                userDTO.setRole("USER");
            }
            userMapper.insertSocialUser(userDTO);
        }
    }

    public boolean validateUser(String userId, String password) {
        try {
            UserDTO user = userMapper.findByUserIdAndProvider(userId, "LOCAL");
            if (user == null) {
                return false;
            }
            return passwordEncoder.matches(password, user.getPassword());
        } catch (Exception e) {
            return false;
        }
    }

    // 소셜 로그인 사용자 처리 (중복 확인 + 등록/업데이트)
    public UserDTO processSocialUser(UserDTO socialUser) {
        System.out.println("[processSocialUser] provider=" + socialUser.getProvider() + ", email=" + socialUser.getEmail() + ", providerId=" + socialUser.getProviderId());
        // 1. provider + provider_id로 먼저 계정 찾기
        UserDTO existingSocialUser = userMapper.findByProviderAndProviderId(socialUser.getProvider(), socialUser.getProviderId());
        if (existingSocialUser != null) {
            System.out.println("[processSocialUser] 기존 소셜 계정 존재: user_id=" + existingSocialUser.getUserId());
            // 이미 해당 소셜 계정이 있으면 정보만 업데이트
            existingSocialUser.setProfileImage(socialUser.getProfileImage());
            userMapper.updateUserSocialInfo(existingSocialUser);
            return existingSocialUser;
        }
        // 2. (email, provider)로 기존 계정 찾기
        UserDTO existingUser = userMapper.findByEmailAndProvider(socialUser.getEmail(), socialUser.getProvider());
        String newUserId = generateSocialUserId(socialUser.getEmail(), socialUser.getProvider());
        System.out.println("[processSocialUser] generateSocialUserId 결과: " + newUserId);
        // (email, provider)로 찾은 계정이 있고, user_id가 새로 만들 id와 같으면 update, 다르면 새로 생성
        if (existingUser != null) {
            System.out.println("[processSocialUser] (email, provider)로 기존 계정 존재: user_id=" + existingUser.getUserId());
            if (existingUser.getUserId().equals(newUserId)) {
                System.out.println("[processSocialUser] user_id 동일, update 진행");
                // 기존 사용자가 있고, user_id도 같으면 update
                existingUser.setProviderId(socialUser.getProviderId());
                existingUser.setProfileImage(socialUser.getProfileImage());
                userMapper.updateUserSocialInfo(existingUser);
                return existingUser;
            } else {
                System.out.println("[processSocialUser] user_id 다름, 새 계정 생성");
                // 기존 계정이 있지만 user_id가 다르면 새 계정 생성
                socialUser.setUserId(newUserId);
                registerUser(socialUser);
                return socialUser;
            }
        } else {
            System.out.println("[processSocialUser] 완전히 새 계정 생성");
            // 완전히 새 계정
            socialUser.setUserId(newUserId);
            registerUser(socialUser);
            return socialUser;
        }
    }

    // 소셜 로그인용 고유 userId 생성
    public String generateSocialUserId(String email, String provider) {
        String baseUserId = email.split("@")[0];
        String userId = provider.toLowerCase() + "_" + baseUserId;
        int counter = 1;
        System.out.println("[generateSocialUserId] 최초 시도: " + userId);
        while (existsByUserId(userId)) {
            userId = provider.toLowerCase() + "_" + baseUserId + counter;
            System.out.println("[generateSocialUserId] 중복 user_id, 재시도: " + userId);
            counter++;
        }
        System.out.println("[generateSocialUserId] 최종 user_id: " + userId);
        return userId;
    }

    // 추가 정보 업데이트 (소셜로그인 후 프로필 완성용)
    public UserDTO updateAdditionalInfo(String userId, Map<String, Object> additionalInfo) {
        UserDTO user = userMapper.findByUserId(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        
        // 핸드폰 갱신
        if (additionalInfo.containsKey("phone")) {
            user.setPhone((String) additionalInfo.get("phone"));
        }
        
        // 기타 필요한 정보들 추가 가능
        // if (additionalInfo.containsKey("address")) {
        //     user.setAddress((String) additionalInfo.get("address"));
        // }
        
        // DB 업데이트
        userMapper.updateUserAdditionalInfo(user);
        
        return user;
    }

    // // 계정 타입 확인 (LOCAL/SOCIAL/HYBRID)
    // public String getAccountType(UserDTO user) {
    //     boolean hasPassword = user.getPassword() != null && !user.getPassword().isEmpty();
    //     boolean hasSocial = user.getProviderId() != null && !"LOCAL".equals(user.getProvider());
        
    //     if (hasPassword && hasSocial) {
    //         return "HYBRID";        // 일반 + 소셜
    //     } else if (hasPassword) {
    //         return "LOCAL";         // 일반만
    //     } else if (hasSocial) {
    //         return "SOCIAL";        // 소셜만
    //     } else {
    //         return "UNKNOWN";       // 오류 상황
    //     }
    // }

    // 소셜 계정에 비밀번호 추가 (SOCIAL → HYBRID)
    // public boolean addPasswordToSocialAccount(String userId, String newPassword) {
    //     UserDTO user = userMapper.findByUserId(userId);
        
    //     if (user == null) {
    //         return false; // 계정이 없음
    //     }
        
    //     // 이미 비밀번호가 있는 경우
    //     if (user.getPassword() != null && !user.getPassword().isEmpty()) {
    //         return false;
    //     }
        
    //     // 소셜 계정이 아닌 경우
    //     if ("LOCAL".equals(user.getProvider()) || user.getProviderId() == null) {
    //         return false;
    //     }
        
    //     // 비밀번호 설정
    //     String encodedPassword = passwordEncoder.encode(newPassword);
    //     user.setPassword(encodedPassword);
        
    //     userMapper.updateUserPassword(user);
    //     return true;
    // }

    // // 일반 계정에 소셜 연동 (LOCAL → HYBRID)
    // @SuppressWarnings("unchecked")
    // public boolean linkSocialToAccount(String userId, String provider, Map<String, Object> socialData) {
    //     UserDTO user = userMapper.findByUserId(userId);
        
    //     if (user == null) {
    //         return false; // 계정이 없음
    //     }
        
    //     // 일반 계정이 아닌 경우
    //     if (!"LOCAL".equals(user.getProvider())) {
    //         return false;
    //     }
        
    //     // 소셜 정보 추출
    //     String providerId;
    //     String profileImage;
        
    //     switch (provider.toUpperCase()) {
    //         case "GOOGLE":
    //             providerId = (String) socialData.get("sub");
    //             profileImage = (String) socialData.get("picture");
    //             break;
    //         case "KAKAO":
    //             providerId = String.valueOf(socialData.get("id"));
    //             Map<String, Object> kakaoAccount = (Map<String, Object>) socialData.get("kakao_account");
    //             Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
    //             profileImage = (String) profile.get("profile_image_url");
    //             break;
    //         case "NAVER":
    //             Map<String, Object> response = (Map<String, Object>) socialData.get("response");
    //             providerId = (String) response.get("id");
    //             profileImage = (String) response.get("profile_image");
    //             break;
    //         default:
    //             return false; // 지원하지 않는 소셜 플랫폼
    //     }
        
    //     // 이미 다른 계정에서 사용 중인 소셜 ID인지 확인
    //     UserDTO existingSocialUser = userMapper.findByProviderAndProviderId(provider.toUpperCase(), providerId);
    //     if (existingSocialUser != null) {
    //         return false; // 이미 다른 계정에서 사용 중
    //     }
        
    //     // 소셜 정보 업데이트
    //     user.setProvider(provider.toUpperCase());
    //     user.setProviderId(providerId);
    //     user.setProfileImage(profileImage);
        
    //     userMapper.updateUserSocialInfo(user);
    //     return true;
    // }

    // 사용자명으로 사용자 ID 조회 (세션 관리용)
    public String getUserIdFromUsername(String username) {
        UserDTO user = userMapper.findByUserId(username);
        return user != null ? user.getUserId() : null;
    }

    // 사용자 목록 조회
    public List<UserDTO> getUserList() {
        return userMapper.getUserList();
    }

    // 사용자 권한 업데이트
    public int updateUserRole(UserDTO user) {
        int affectedRows = userMapper.updateUserRole(user);
        return affectedRows;
    }

    // 비밀번호 변경 (일반 로그인)
    public boolean changePassword(String userId, String currentPassword, String newPassword) {
        UserDTO user = userMapper.findByUserId(userId);
        if (user == null || user.getPassword() == null) return false;
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) return false;
        String encoded = passwordEncoder.encode(newPassword);
        user.setPassword(encoded);
        userMapper.updateUserPassword(user);
        return true;
    }
}