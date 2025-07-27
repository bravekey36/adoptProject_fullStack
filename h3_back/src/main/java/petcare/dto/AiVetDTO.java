package petcare.dto;

import java.time.LocalDateTime;

public class AiVetDTO {
    // chatbot_aivet_session 테이블용
    private String sessionId;      // session_id (VARCHAR 36)
    private String userId;         // user_id (VARCHAR 20)
    private String summary;        // summary (TEXT)
    private String status;         // status (ENUM)
    private LocalDateTime createdAt;   // created_at
    private LocalDateTime updatedAt;   // updated_at

    public AiVetDTO() {}

    // Getter/Setter
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}