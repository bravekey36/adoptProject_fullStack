package petcare.dto;

import java.time.LocalDateTime;
import java.util.List;

public class MessageDTO {
    // chatbot_aivet_message 테이블용
    private int messageId;         // message_id (INT AUTO_INCREMENT)
    private String sessionId;      // session_id (VARCHAR 36)
    private String senderType;     // sender_type (ENUM 'user'/'ai')
    private String content;        // content (TEXT)
    private LocalDateTime createdAt;   // created_at (DATETIME)
    private List<ImageDTO> images;      // 해당 메시지의 이미지 목록

    public MessageDTO() {}

    // Getter/Setter
    public int getMessageId() { return messageId; }
    public void setMessageId(int messageId) { this.messageId = messageId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<ImageDTO> getImages() { return images; }
    public void setImages(List<ImageDTO> images) { this.images = images; }
}