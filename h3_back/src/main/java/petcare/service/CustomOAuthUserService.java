package petcare.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import petcare.dto.UserDTO;
import petcare.mapper.UserMapper;
import petcare.security.oauth.OAuthUserInfo;
import petcare.security.oauth.OAuthUserInfoFactory;
import petcare.security.oauth.CustomOAuthUser;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuthUserService extends DefaultOAuth2UserService {
    
    private final UserMapper userMapper;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("=== CustomOAuthUserService.loadUser 진입 ===");
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        System.out.println("[DEBUG] registrationId: " + registrationId);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        try {
            ObjectMapper mapper = new ObjectMapper();
            String attrJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(attributes);
            System.out.println("[DEBUG] attributes 전체: " + attrJson);
        } catch (Exception e) {
            System.out.println("[DEBUG] attributes JSON 변환 실패: " + e.getMessage());
        }
        // 네이버는 attributes를 response로 교체
        if ("naver".equals(registrationId)) {
            Object responseObj = attributes.get("response");
            System.out.println("[DEBUG] 네이버 attributes.get('response'): " + responseObj);
            if (responseObj instanceof Map) {
                Map<String, Object> response = (Map<String, Object>) responseObj;
                Object idObj = response.get("id");
                System.out.println("[DEBUG] 네이버 response.get('id'): " + idObj);
                if (idObj == null) {
                    System.out.println("[ERROR] 네이버 id가 null입니다. 임시로 'no-id' 반환");
                    response.put("id", "no-id");
                }
                // 네이버 response로 OAuthUserInfo 생성
                OAuthUserInfo oAuthUserInfo = OAuthUserInfoFactory.get(registrationId, response);
                UserDTO userDTO = processOAuth2User(oAuthUserInfo, registrationId);
                return new CustomOAuthUser(oAuth2User, userDTO);
            } else {
                System.out.println("[ERROR] 네이버 response가 Map이 아님: " + responseObj);
                Map<String, Object> dummy = new java.util.HashMap<>();
                dummy.put("id", "no-id");
                OAuthUserInfo oAuthUserInfo = OAuthUserInfoFactory.get(registrationId, dummy);
                UserDTO userDTO = processOAuth2User(oAuthUserInfo, registrationId);
                return new CustomOAuthUser(oAuth2User, userDTO);
            }
        }
        // 구글 등 기타 provider
        OAuthUserInfo oAuthUserInfo = OAuthUserInfoFactory.get(registrationId, attributes);
        UserDTO userDTO = processOAuth2User(oAuthUserInfo, registrationId);
        return new CustomOAuthUser(oAuth2User, userDTO);
    }
    
    private UserDTO processOAuth2User(OAuthUserInfo oauth2UserInfo, String provider) {
        // --- DEBUG: userInfo 타입, 내용, getEmail() 결과 모두 출력 ---
        System.out.println("[DEBUG] processOAuth2User() provider: " + provider);
        System.out.println("[DEBUG] oauth2UserInfo class: " + oauth2UserInfo.getClass().getName());
        System.out.println("[DEBUG] oauth2UserInfo toString: " + oauth2UserInfo.toString());
        String email = oauth2UserInfo.getEmail();
        System.out.println("[DEBUG] oauth2UserInfo.getEmail(): " + email);
        if ("naver".equals(provider)) {
            try {
                Class<?> clazz = oauth2UserInfo.getClass();
                System.out.println("[DEBUG] (naver) oauth2UserInfo class: " + clazz.getName());
                if (clazz.getSimpleName().equals("NaverUserInfo")) {
                    petcare.security.oauth.NaverUserInfo naverUserInfo = (petcare.security.oauth.NaverUserInfo) oauth2UserInfo;
                    String naverEmail = naverUserInfo.getEmail();
                    System.out.println("[DEBUG] (naver) NaverUserInfo.getEmail(): " + naverEmail);
                }
            } catch (Exception e) {
                System.out.println("[ERROR] (naver) NaverUserInfo 강제 캐스팅/호출 실패: " + e.getMessage());
            }
        }
        if (email == null || email.isEmpty()) {
            System.out.println("[ERROR] 소셜 로그인에서 email이 null입니다. provider=" + provider + ", userInfo=" + oauth2UserInfo.getAttributes());
            throw new RuntimeException("소셜 로그인에 이메일 정보가 필요합니다. 제공 동의 여부를 확인하세요.");
        }
        // 이메일로 기존 사용자 확인
        UserDTO existingUser = userMapper.findByEmail(email);
        System.out.println("[DEBUG] 재로그인 findByEmail 결과: " + existingUser);
        if (existingUser != null) {
            System.out.println("[DEBUG] 재로그인 existingUser.userId: " + existingUser.getUserId());
            // userId는 절대 변경하지 않음!
            existingUser.setProvider(provider);
            existingUser.setProviderId(oauth2UserInfo.getId());
            existingUser.setProfileImage(oauth2UserInfo.getImageUrl());
            userMapper.updateUserSocialInfo(existingUser);
            return existingUser;
        } else {
            // 신규 가입자만 userId 생성
            UserDTO newUser = oauth2UserInfo.toUserDTO(provider);
            String newUserId = generateUniqueUserId(email, provider);
            newUser.setUserId(newUserId);
            userMapper.insertSocialUser(newUser);
            return newUser;
        }
    }

    private String generateUniqueUserId(String email, String provider) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("userId 생성에 필요한 email이 null입니다.");
        }
        // provider_이메일앞부분 형태로 생성, 중복되면 숫자 추가
        String baseUserId = provider + "_" + email.split("@")[0];
        String userId = baseUserId;
        int counter = 1;
        while (userMapper.findByUserId(userId) != null) {
            userId = baseUserId + counter;
            counter++;
        }
        return userId;
    }
    
    private void insertSocialUser(UserDTO user) {
        // 소셜로그인 사용자 전용 insert 메서드 (password 없이)
        userMapper.insertSocialUser(user);
    }
    
    private void updateUser(UserDTO user) {
        // 사용자 정보 업데이트
        userMapper.updateUserSocialInfo(user);
    }
}
