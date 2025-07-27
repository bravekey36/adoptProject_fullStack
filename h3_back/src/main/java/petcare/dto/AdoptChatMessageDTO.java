package petcare.dto;

import java.time.LocalDateTime;

public class AdoptChatMessageDTO {
    private Long messageId;        // 메시지 ID
    private String sessionId;      // 세션 ID
    private String messageType;    // 메시지 타입 (USER, BOT)
    private String content;        // 메시지 내용
    private String petsJson;       // 추천 보호동물 정보 (JSON)
    private LocalDateTime createdAt; // 생성일시
    private Integer sequence;      // 세션 내 메시지 순서

    public AdoptChatMessageDTO() {}

    // Constructor for user message (without sequence)
    public AdoptChatMessageDTO(String sessionId, String messageType, String content) {
        this.sessionId = sessionId;
        this.messageType = messageType;
        this.content = content;
    }

    // Constructor for user message
    public AdoptChatMessageDTO(String sessionId, String messageType, String content, Integer sequence) {
        this.sessionId = sessionId;
        this.messageType = messageType;
        this.content = content;
        this.sequence = sequence;
    }

    // Constructor for bot message with pets data (without sequence)
    public AdoptChatMessageDTO(String sessionId, String messageType, String content, String petsJson) {
        this.sessionId = sessionId;
        this.messageType = messageType;
        this.content = content;
        this.petsJson = petsJson;
    }

    // Constructor for bot message with pets data
    public AdoptChatMessageDTO(String sessionId, String messageType, String content, String petsJson, Integer sequence) {
        this.sessionId = sessionId;
        this.messageType = messageType;
        this.content = content;
        this.petsJson = petsJson;
        this.sequence = sequence;
    }

    // Getters and Setters
    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPetsJson() {
        return petsJson;
    }

    public void setPetsJson(String petsJson) {
        this.petsJson = petsJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    @Override
    public String toString() {
        return "AdoptChatMessageDTO{" +
                "messageId=" + messageId +
                ", sessionId='" + sessionId + '\'' +
                ", messageType='" + messageType + '\'' +
                ", content='" + content + '\'' +
                ", petsJson='" + petsJson + '\'' +
                ", createdAt=" + createdAt +
                ", sequence=" + sequence +
                '}';
    }
}
