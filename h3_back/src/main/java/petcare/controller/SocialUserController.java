package petcare.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import petcare.dto.UserDTO;
import petcare.service.UserService;
import petcare.security.jwt.JwtProvider;

@RestController
@RequestMapping("/api/social")
public class SocialUserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtProvider jwtProvider;

    // 소셜 로그인 관련 API 구현
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Social Login Controller is working!");
    }

    // Google 회원가입 처리
    @PostMapping("/google/signup")
    public ResponseEntity<Map<String, Object>> googleSignup(@RequestBody Map<String, Object> googleData) {
        try {
            UserDTO socialUser = createUserDTOFromGoogle(googleData);
            return processSocialSignup(socialUser);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Google 회원가입 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Google 로그인 처리
    @PostMapping("/google/login")
    public ResponseEntity<Map<String, Object>> googleLogin(@RequestBody Map<String, Object> googleData) {
        try {
            UserDTO socialUser = createUserDTOFromGoogle(googleData);
            return processSocialLogin(socialUser);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Google 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Kakao 회원가입 처리
    @PostMapping("/kakao/signup")
    public ResponseEntity<Map<String, Object>> kakaoSignup(@RequestBody Map<String, Object> kakaoData) {
        try {
            UserDTO socialUser = createUserDTOFromKakao(kakaoData);
            return processSocialSignup(socialUser);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Kakao 회원가입 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Kakao 로그인 처리
    @PostMapping("/kakao/login")
    public ResponseEntity<Map<String, Object>> kakaoLogin(@RequestBody Map<String, Object> kakaoData) {
        try {
            UserDTO socialUser = createUserDTOFromKakao(kakaoData);
            return processSocialLogin(socialUser);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Kakao 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Naver 회원가입 처리
    @PostMapping("/naver/signup")
    public ResponseEntity<Map<String, Object>> naverSignup(@RequestBody Map<String, Object> naverData) {
        try {
            UserDTO socialUser = createUserDTOFromNaver(naverData);
            return processSocialSignup(socialUser);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Naver 회원가입 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Naver 로그인 처리
    @PostMapping("/naver/login")
    public ResponseEntity<Map<String, Object>> naverLogin(@RequestBody Map<String, Object> naverData) {
        try {
            UserDTO socialUser = createUserDTOFromNaver(naverData);
            return processSocialLogin(socialUser);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Naver 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 소셜 회원가입 처리 로직 (타협안: 이메일 중복 시 차단)
    private ResponseEntity<Map<String, Object>> processSocialSignup(UserDTO socialUser) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1단계: 이메일 중복 체크 
            UserDTO existingUser = userService.findByEmail(socialUser.getEmail());
            if (existingUser != null) {
                response.put("success", false);
                response.put("message", "이미 가입된 이메일입니다. 다른 이메일을 사용해주세요.");
                response.put("suggestion", "기존 계정으로 로그인하거나 다른 이메일을 사용해주세요.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 2단계: UserId 생성
            if (socialUser.getUserId() == null) {
                socialUser.setUserId(userService.generateSocialUserId(socialUser.getEmail(), socialUser.getProvider()));
            }
            
            // 3단계: 신규 사용자 등록만 허용
            userService.registerUser(socialUser);
            
            // 4단계: JWT 토큰 발급
            String token = jwtProvider.create(socialUser.getUserId(), socialUser.getRole());
            
            response.put("success", true);
            response.put("token", token);
            response.put("userId", socialUser.getUserId());
            response.put("name", socialUser.getName());
            response.put("email", socialUser.getEmail());
            response.put("provider", socialUser.getProvider());
            response.put("message", "소셜 회원가입이 완료되었습니다.");
            
            // 추가 정보 입력 필요 여부 확인
            boolean needsAdditionalInfo = checkNeedsAdditionalInfo(socialUser);
            response.put("needsAdditionalInfo", needsAdditionalInfo);
            
            if (needsAdditionalInfo) {
                response.put("step", "additional_info");
                response.put("additionalMessage", "추가 정보를 입력해주세요.");
                response.put("missingFields", getMissingFields(socialUser));
            } else {
                response.put("step", "complete");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "소셜 회원가입 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 소셜 로그인 처리 로직 (기존 계정만 허용)
    private ResponseEntity<Map<String, Object>> processSocialLogin(UserDTO socialUser) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1단계: 기존 계정 확인 (소셜 로그인은 기존 계정만 허용)
            UserDTO existingUser = userService.findByEmail(socialUser.getEmail());
            if (existingUser == null) {
                response.put("success", false);
                response.put("message", "가입되지 않은 이메일입니다. 먼저 회원가입을 해주세요.");
                response.put("suggestion", "소셜 회원가입을 진행해주세요.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 2단계: 소셜 계정 연동 확인
            if (!socialUser.getProvider().equals(existingUser.getProvider())) {
                response.put("success", false);
                response.put("message", "다른 방식으로 가입된 계정입니다.");
                response.put("currentProvider", existingUser.getProvider());
                response.put("attemptedProvider", socialUser.getProvider());
                
                if ("LOCAL".equals(existingUser.getProvider())) {
                    response.put("suggestion", "일반 로그인을 사용하거나 마이페이지에서 소셜 계정을 연동해주세요.");
                } else {
                    response.put("suggestion", existingUser.getProvider() + " 로그인을 사용해주세요.");
                }
                return ResponseEntity.badRequest().body(response);
            }
            
            // 3단계: provider_id 확인
            if (!socialUser.getProviderId().equals(existingUser.getProviderId())) {
                response.put("success", false);
                response.put("message", "소셜 계정 정보가 일치하지 않습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 4단계: 로그인 성공 - JWT 토큰 발급
            String token = jwtProvider.create(existingUser.getUserId(), existingUser.getRole());
            
            response.put("success", true);
            response.put("token", token);
            response.put("userId", existingUser.getUserId());
            response.put("name", existingUser.getName());
            response.put("email", existingUser.getEmail());
            response.put("provider", existingUser.getProvider());
            response.put("message", "로그인 성공");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "소셜 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 추가 정보 필요 여부 확인
    private boolean checkNeedsAdditionalInfo(UserDTO user) {
        return user.getPhone() == null || user.getPhone().trim().isEmpty();
    }

    // 누락된 필드 목록 반환
    private String[] getMissingFields(UserDTO user) {
        java.util.List<String> missing = new java.util.ArrayList<>();
        
        if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            missing.add("phone");
        }
        
        return missing.toArray(new String[0]);
    }

    // 추가 정보 입력 완료 처리
    @PostMapping("/complete-profile")
    public ResponseEntity<Map<String, Object>> completeProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> additionalInfo) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // JWT 토큰에서 userId 추출
            String token = authHeader.replace("Bearer ", "");
            String userId = jwtProvider.getUserId(token);
            
            // 추가 정보 업데이트
            UserDTO updatedUser = userService.updateAdditionalInfo(userId, additionalInfo);
            
            response.put("success", true);
            response.put("message", "프로필 정보가 완성되었습니다.");
            response.put("step", "complete");
            response.put("user", updatedUser);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "프로필 완성 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Google 데이터를 UserDTO로 변환
    private UserDTO createUserDTOFromGoogle(Map<String, Object> googleData) {
        UserDTO user = new UserDTO();
        user.setEmail((String) googleData.get("email"));
        user.setName((String) googleData.get("name"));
        user.setProvider("GOOGLE");
        user.setProviderId((String) googleData.get("sub"));
        user.setProfileImage((String) googleData.get("picture"));
        return user;
    }

    // Kakao 데이터를 UserDTO로 변환
    @SuppressWarnings("unchecked")
    private UserDTO createUserDTOFromKakao(Map<String, Object> kakaoData) {
        UserDTO user = new UserDTO();
        
        // Kakao는 kakao_account 안에 정보가 있음
        Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoData.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        
        user.setEmail((String) kakaoAccount.get("email"));
        user.setName((String) profile.get("nickname"));
        user.setProvider("KAKAO");
        user.setProviderId(String.valueOf(kakaoData.get("id")));
        user.setProfileImage((String) profile.get("profile_image_url"));
        return user;
    }

    // Naver 데이터를 UserDTO로 변환
    @SuppressWarnings("unchecked")
    private UserDTO createUserDTOFromNaver(Map<String, Object> naverData) {
        UserDTO user = new UserDTO();
        
        // Naver는 response 안에 정보가 있음
        Map<String, Object> response = (Map<String, Object>) naverData.get("response");
        
        user.setEmail((String) response.get("email"));
        user.setName((String) response.get("name"));
        user.setProvider("NAVER");
        user.setProviderId((String) response.get("id"));
        user.setProfileImage((String) response.get("profile_image"));
        return user;
    }
}
