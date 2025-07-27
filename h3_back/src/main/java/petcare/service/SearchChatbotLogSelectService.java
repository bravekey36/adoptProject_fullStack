package petcare.service;

import lombok.RequiredArgsConstructor;
import petcare.dto.SearchChatbotLogSelectDTO;
import petcare.mapper.SearchChatbotLogSelectMapper;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchChatbotLogSelectService {
    
    private final SearchChatbotLogSelectMapper searchChatbotLogSelectMapper;


    public List<SearchChatbotLogSelectDTO> getSessionList() {
        return searchChatbotLogSelectMapper.selectNowOpendSession();
    }
}
