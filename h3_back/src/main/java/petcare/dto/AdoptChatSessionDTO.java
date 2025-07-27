package petcare.dto;

import java.time.LocalDateTime;

public class AdoptChatSessionDTO {
    private String sessionId;      // 세션 ID (UUID)
    private String userId;         // 사용자 ID
    private String sessionTitle;   // 세션 제목
    private LocalDateTime createdAt; // 생성일시
    private LocalDateTime updatedAt; // 수정일시
    private LocalDateTime expiresAt; // 만료일시
    private String status;         // 세션 상태 (ACTIVE, CLOSED)

    public AdoptChatSessionDTO() {}

    // Constructor for creating new session
    public AdoptChatSessionDTO(String sessionId, String userId, String sessionTitle) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.sessionTitle = sessionTitle;
        this.status = "ACTIVE";
    }

    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSessionTitle() {
        return sessionTitle;
    }

    public void setSessionTitle(String sessionTitle) {
        this.sessionTitle = sessionTitle;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "AdoptChatSessionDTO{" +
                "sessionId='" + sessionId + '\'' +
                ", userId=" + userId +
                ", sessionTitle='" + sessionTitle + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", expiresAt=" + expiresAt +
                ", status='" + status + '\'' +
                '}';
    }
}
