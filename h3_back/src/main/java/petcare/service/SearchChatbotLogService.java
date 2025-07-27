package petcare.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import petcare.dto.SearchChatbotLogDTO;
import petcare.mapper.SearchChatbotLogMapper;

@Service
public class SearchChatbotLogService {
    
    @Autowired 
    SearchChatbotLogMapper searchChatbotLogMapper;

    public int savesearchChatbotLog(SearchChatbotLogDTO searchChatbotLogDTO) {
        return searchChatbotLogMapper.insertSearchChatbotLog(searchChatbotLogDTO);
    }

    // 세션 상태 close로 변경하는 메서드 추가
    public int closeSessionBySessionId(String sessionId) {
        return searchChatbotLogMapper.updateSessionStatusToClose(sessionId);
    }
}
