package petcare.service;

import petcare.dto.SearchChatbotHistoryDTO;
import petcare.mapper.SearchChatbotHistoryMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchChatbotHistoryService {
    private final SearchChatbotHistoryMapper searchChatbotHistoryMapper;

    public List<SearchChatbotHistoryDTO> getRecentOpenChatLogs(String status, int limit) {
        return searchChatbotHistoryMapper.getLatestOpenChatLogs(status, limit);
    }
}