package petcare.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserDTO {
    private String userId;
    private String password;
    private String email;
    private String phone;
    private String name;
    private String role;
    private String provider;        // 추가된 필드
    private String providerId;      // 추가된 필드  
    private String profileImage;    // 추가된 필드
    private LocalDateTime createdAt;   // 생성일시
    private LocalDateTime updatedAt;   // 수정일시

    // 기본 생성자
    public UserDTO() {}

    // 모든 필드를 받는 생성자
    public UserDTO(String userId, String password, String email, String phone, String name, String role) {
        this.userId = userId;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.name = name;
        this.role = role;
        this.provider = "LOCAL";  // 기본값
    }

    // 확장된 생성자
    public UserDTO(String userId, String password, String email, String phone, String name, String role, 
                   String provider, String providerId, String profileImage) {
        this.userId = userId;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.name = name;
        this.role = role;
        this.provider = provider;
        this.providerId = providerId;
        this.profileImage = profileImage;
    }

    // 일부 필드만 받는 생성자 (예시)
    public UserDTO(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }


}