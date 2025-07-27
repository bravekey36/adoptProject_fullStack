package petcare.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import petcare.dto.UserDTO;
import petcare.dto.LoginResponse;
import petcare.service.UserService;
import petcare.security.jwt.JwtProvider;

@RestController
@RequestMapping("/api/user")
public class UserController {
    // 마이페이지: 회원정보(전화번호 등) 수정 (PATCH /api/user/info, /api/user/phone 모두 지원)
    @PatchMapping({"/info", "/phone"})
    public ResponseEntity<Map<String, Object>> updateUserInfo(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> req) {
        Map<String, Object> res = new HashMap<>();
        String token = authHeader.replace("Bearer ", "");
        String userId = jwtProvider.getUserId(token);

        // 예시: 전화번호만 수정 (필요시 다른 필드도 추가)
        String phone = (String) req.get("phone");
        if (phone == null || phone.trim().isEmpty()) {
            res.put("success", false);
            res.put("message", "전화번호를 입력해주세요.");
            return ResponseEntity.badRequest().body(res);
        }
        boolean result = userService.updateUserPhone(userId, phone);
        if (result) {
            res.put("success", true);
            res.put("message", "전화번호가 변경되었습니다.");
            return ResponseEntity.ok(res);
        } else {
            res.put("success", false);
            res.put("message", "전화번호 변경에 실패했습니다.");
            return ResponseEntity.badRequest().body(res);
        }
    }
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtProvider jwtProvider;

    // 기존: 특정 userId로 조회
    @GetMapping("/{userId}")
    public UserDTO getUser(@PathVariable String userId) {
        return userService.findByUserId(userId);
    }

    // 추가: 인증된 사용자 본인 정보 조회 (프론트엔드 /api/user/me 요청 대응)
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMyInfo(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String userId = jwtProvider.getUserId(token);
        UserDTO user = userService.findByUserId(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping("/register")
    public String registerUser(@RequestBody UserDTO user) {
        userService.registerUser(user);
        return "회원가입 성공";
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@RequestBody UserDTO loginRequest) {
        boolean isValid = userService.validateUser(loginRequest.getUserId(), loginRequest.getPassword());
        if (isValid) {
            UserDTO user = userService.findByUserId(loginRequest.getUserId());
            // JWT 토큰 생성 - 사용자의 실제 role 사용
            String token = jwtProvider.create(user.getUserId(), user.getRole());
            LoginResponse response = LoginResponse.success(token, user.getUserId(), user.getName(), user.getRole());
            return ResponseEntity.ok(response);
        } else {
            LoginResponse response = LoginResponse.failure("로그인 실패: 아이디 또는 비밀번호가 잘못되었습니다.");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/check-duplicate/{userId}")
    public ResponseEntity<String> checkDuplicateUserId(@PathVariable String userId) {
        boolean exists = userService.existsByUserId(userId);
        if (exists) {
            return ResponseEntity.ok("duplicate");
        } else {
            return ResponseEntity.ok("available");
        }
    }
    
    @GetMapping("/check-duplicate-email/{email}")
    public ResponseEntity<String> checkDuplicateEmail(@PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        if (exists) {
            return ResponseEntity.ok("duplicate");
        } else {
            return ResponseEntity.ok("available");
        }
    }

    // // 마이페이지: 계정 정보 조회
    // @GetMapping("/account-info")
    // public ResponseEntity<Map<String, Object>> getAccountInfo(@RequestHeader("Authorization") String authHeader) {
    //     Map<String, Object> response = new HashMap<>();
        
    //     try {
    //         // JWT 토큰에서 userId 추출
    //         String token = authHeader.replace("Bearer ", "");
    //         String userId = jwtProvider.getUserId(token);
            
    //         UserDTO user = userService.findByUserId(userId);
    //         if (user == null) {
    //             response.put("success", false);
    //             response.put("message", "사용자 정보를 찾을 수 없습니다.");
    //             return ResponseEntity.badRequest().body(response);
    //         }
            
    //         // 계정 타입 확인
    //         // String accountType = userService.getAccountType(user);
            
    //         response.put("success", true);
    //         response.put("userId", user.getUserId());
    //         response.put("email", user.getEmail());
    //         response.put("name", user.getName());
    //         response.put("phone", user.getPhone());
    //         response.put("provider", user.getProvider());
    //         // response.put("accountType", accountType);
            
    //         // 계정 타입별 사용 가능한 기능
    //         switch (accountType) {
    //             case "LOCAL":
    //                 response.put("canAddSocial", true);      // 소셜 연동 가능
    //                 response.put("canAddPassword", false);   // 이미 비밀번호 있음
    //                 response.put("availableFeatures", "소셜 계정 연동이 가능합니다.");
    //                 break;
    //             case "SOCIAL":
    //                 response.put("canAddSocial", false);     // 이미 소셜 연동됨
    //                 response.put("canAddPassword", true);    // 비밀번호 설정 가능
    //                 response.put("availableFeatures", "비밀번호를 설정하여 일반 로그인도 사용할 수 있습니다.");
    //                 break;
    //             case "HYBRID":
    //                 response.put("canAddSocial", false);     // 이미 모든 기능 있음
    //                 response.put("canAddPassword", false);
    //                 response.put("availableFeatures", "모든 로그인 방식을 사용할 수 있습니다.");
    //                 break;
    //             default:
    //                 response.put("canAddSocial", false);
    //                 response.put("canAddPassword", false);
    //                 response.put("availableFeatures", "알 수 없는 계정 상태입니다.");
    //         }
            
    //         return ResponseEntity.ok(response);
    //     } catch (Exception e) {
    //         response.put("success", false);
    //         response.put("message", "계정 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
    //         return ResponseEntity.badRequest().body(response);
    //     }
    // }

    // 마이페이지: 소셜 계정에 비밀번호 추가 (SOCIAL → HYBRID)
    // @PostMapping("/add-password")
    // public ResponseEntity<Map<String, Object>> addPassword(
    //         @RequestHeader("Authorization") String authHeader,
    //         @RequestBody Map<String, Object> request) {
        
    //     Map<String, Object> response = new HashMap<>();
        
    //     try {
    //         // JWT 토큰에서 userId 추출
    //         String token = authHeader.replace("Bearer ", "");
    //         String userId = jwtProvider.getUserId(token);
            
    //         String newPassword = (String) request.get("password");
    //         if (newPassword == null || newPassword.trim().isEmpty()) {
    //             response.put("success", false);
    //             response.put("message", "비밀번호를 입력해주세요.");
    //             return ResponseEntity.badRequest().body(response);
    //         }
            
    //         // 소셜 계정에 비밀번호 추가
    //         boolean success = userService.addPasswordToSocialAccount(userId, newPassword);
            
    //         if (success) {
    //             response.put("success", true);
    //             response.put("message", "비밀번호가 설정되었습니다. 이제 일반 로그인도 사용할 수 있습니다.");
    //             response.put("newAccountType", "HYBRID");
    //         } else {
    //             response.put("success", false);
    //             response.put("message", "비밀번호 설정에 실패했습니다. 이미 비밀번호가 설정되어 있거나 소셜 계정이 아닙니다.");
    //         }
            
    //         return ResponseEntity.ok(response);
    //     } catch (Exception e) {
    //         response.put("success", false);
    //         response.put("message", "비밀번호 설정 중 오류가 발생했습니다: " + e.getMessage());
    //         return ResponseEntity.badRequest().body(response);
    //     }
    // }

    // // 마이페이지: 일반 계정에 소셜 연동 (LOCAL → HYBRID)
    // @PostMapping("/link-social/{provider}")
    // public ResponseEntity<Map<String, Object>> linkSocial(
    //         @RequestHeader("Authorization") String authHeader,
    //         @PathVariable String provider,
    //         @RequestBody Map<String, Object> socialData) {
        
    //     Map<String, Object> response = new HashMap<>();
        
    //     try {
    //         // JWT 토큰에서 userId 추출
    //         String token = authHeader.replace("Bearer ", "");
    //         String userId = jwtProvider.getUserId(token);
            
    //         // 소셜 연동 처리
    //         boolean success = userService.linkSocialToAccount(userId, provider, socialData);
            
    //         if (success) {
    //             response.put("success", true);
    //             response.put("message", provider + " 계정이 연동되었습니다. 이제 소셜 로그인도 사용할 수 있습니다.");
    //             response.put("newAccountType", "HYBRID");
    //             response.put("linkedProvider", provider);
    //         } else {
    //             response.put("success", false);
    //             response.put("message", "소셜 계정 연동에 실패했습니다. 이미 다른 계정에서 사용 중이거나 잘못된 정보입니다.");
    //         }
            
    //         return ResponseEntity.ok(response);
    //     } catch (Exception e) {
    //         response.put("success", false);
    //         response.put("message", "소셜 계정 연동 중 오류가 발생했습니다: " + e.getMessage());
    //         return ResponseEntity.badRequest().body(response);
    //     }
    // }

    // 사용자 목록 조회
    @GetMapping("/list")
    public List<UserDTO> getUserList() {
        return userService.getUserList();
    }

    // 사용자 권한 업데이트
    @PostMapping("/update-role")
    public ResponseEntity<Map<String, Object>> updateUserRole(
            @RequestBody UserDTO user) {
        Map<String, Object> resultMap = new HashMap<>();
        int affectedRows = userService.updateUserRole(user);
        resultMap.put("success", affectedRows > 0);
        resultMap.put("affectedRows", affectedRows);
        return ResponseEntity.ok(resultMap);
    }

    // 비밀번호 변경 (일반 로그인)
    @PatchMapping("/password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> req) {
        Map<String, Object> res = new HashMap<>();
        String token = authHeader.replace("Bearer ", "");
        String userId = jwtProvider.getUserId(token);
        String currentPassword = req.get("currentPassword");
        String newPassword = req.get("newPassword");
        boolean result = userService.changePassword(userId, currentPassword, newPassword);
        if (result) {
            res.put("success", true);
            res.put("message", "비밀번호가 변경되었습니다.");
            return ResponseEntity.ok(res);
        } else {
            res.put("success", false);
            res.put("message", "현재 비밀번호가 일치하지 않거나 변경에 실패했습니다.");
            return ResponseEntity.badRequest().body(res);
        }
    }

    // 관리자 권한 테스트용 API
    @GetMapping("/admin-test")
    public ResponseEntity<Map<String, Object>> adminTest(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> res = new HashMap<>();
        String token = authHeader.replace("Bearer ", "");
        String userId = jwtProvider.getUserId(token);
        String role = jwtProvider.getRole(token);
        
        res.put("success", true);
        res.put("message", "관리자 권한 테스트 성공");
        res.put("userId", userId);
        res.put("role", role);
        res.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(res);
    }
}