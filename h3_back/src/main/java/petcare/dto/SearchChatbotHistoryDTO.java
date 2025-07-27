package petcare.dto;

import lombok.Data;

@Data
public class SearchChatbotHistoryDTO {
    
    private String sessionId;
    private String senderType; // user 또는 ai
    private String message;
}
