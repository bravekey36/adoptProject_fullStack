package petcare.mapper;

import org.apache.ibatis.annotations.Mapper;
import petcare.dto.SearchChatbotLogDTO;

@Mapper
public interface SearchChatbotLogMapper {

    int insertSearchChatbotLog(SearchChatbotLogDTO searchChatbotLogDTO);

    int updateSessionStatusToClose(String sessionId);
} 
