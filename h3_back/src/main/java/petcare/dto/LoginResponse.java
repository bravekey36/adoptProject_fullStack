package petcare.dto;

public class LoginResponse {
    private boolean success;
    private String message;
    private String token;
    private String userId;
    private String name;
    private String role;
    
    // 기본 생성자
    public LoginResponse() {}
    
    // 생성자
    public LoginResponse(boolean success, String message, String token, String userId, String name, String role) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.role = role;
    }
    
    // 성공 응답 생성 메서드
    public static LoginResponse success(String token, String userId, String name, String role) {
        return new LoginResponse(true, "로그인 성공", token, userId, name, role);
    }
    
    // 실패 응답 생성 메서드
    public static LoginResponse failure(String message) {
        return new LoginResponse(false, message, null, null, null, null);
    }
    
    // Getter & Setter
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
}
