package petcare.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import petcare.dto.AdoptChatSessionDTO;
import petcare.dto.AdoptChatMessageDTO;

import java.util.List;

@Mapper
public interface AdoptChatMapper {
    
    // ============ 세션 관련 ============
    
    /**
     * 새 세션 생성
     */
    int insertSession(AdoptChatSessionDTO session);
    
    /**
     * 사용자별 세션 목록 조회 (최신순)
     */
    List<AdoptChatSessionDTO> selectSessionsByUserId(@Param("userId") String userId);
    
    /**
     * 세션 ID로 세션 조회
     */
    AdoptChatSessionDTO selectSessionById(@Param("sessionId") String sessionId);
    
    /**
     * 세션 상태 업데이트
     */
    int updateSessionStatus(@Param("sessionId") String sessionId, @Param("status") String status);
    
    /**
     * 세션 제목 업데이트
     */
    int updateSessionTitle(@Param("sessionId") String sessionId, @Param("title") String title);
    
    /**
     * 세션 updated_at 갱신
     */
    int updateSessionTimestamp(@Param("sessionId") String sessionId);
    
    /**
     * 만료된 세션 삭제 (30일 정리용)
     */
    int deleteExpiredSessions();
    
    // ============ 메시지 관련 ============
    
    /**
     * 새 메시지 저장
     */
    int insertMessage(AdoptChatMessageDTO message);
    
    /**
     * 세션의 모든 메시지 조회 (순서대로)
     */
    List<AdoptChatMessageDTO> selectMessagesBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 세션의 메시지 개수 조회
     */
    int countMessagesBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 세션의 다음 sequence 번호 조회
     */
    Integer selectNextSequence(@Param("sessionId") String sessionId);
    
    /**
     * 세션의 첫 번째 사용자 메시지 조회 (제목 생성용)
     */
    AdoptChatMessageDTO selectFirstUserMessage(@Param("sessionId") String sessionId);
}
