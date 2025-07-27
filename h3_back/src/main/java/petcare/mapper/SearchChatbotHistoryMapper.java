package petcare.mapper;

import petcare.dto.SearchChatbotHistoryDTO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SearchChatbotHistoryMapper {
    
    @Select("""
        SELECT session_id AS sessionId, sender_type AS senderType, message
        FROM chatbot_aisearch_message
        WHERE session_status = #{status}
        ORDER BY created_at DESC
        LIMIT #{limit}
    """)
    List<SearchChatbotHistoryDTO> getLatestOpenChatLogs(

        @Param("status") String status,
        @Param("limit") int limit
    );
}
