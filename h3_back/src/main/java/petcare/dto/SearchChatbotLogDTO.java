package petcare.dto;

import lombok.Data;

@Data
public class SearchChatbotLogDTO {
    private String sessionId;
    private String senderType;
    private String message;
}
