package petcare.mapper;

import petcare.dto.SearchChatbotLogSelectDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SearchChatbotLogSelectMapper {
    
    List<SearchChatbotLogSelectDTO> selectNowOpendSession();
}
