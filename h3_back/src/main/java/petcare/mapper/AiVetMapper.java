package petcare.mapper;

import petcare.dto.AiVetDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

import petcare.dto.MessageDTO;
import petcare.dto.ImageDTO;  


@Mapper
public interface AiVetMapper {
    
    // DB 연결 테스트용 - 세션 생성
    int insertSession(AiVetDTO session);
    
    // DB 연결 테스트용 - 세션 조회
    AiVetDTO selectSessionById(@Param("sessionId") String sessionId);
    
    // DB 연결 테스트용 - 전체 세션 조회 (최신 5개)
    List<AiVetDTO> selectRecentSessions();

    // 메시지 저장
    int insertMessage(MessageDTO message);
    
    // 특정 세션의 메시지 조회 (시간순)
    List<MessageDTO> selectMessagesBySessionId(@Param("sessionId") String sessionId);
    
    // 메시지 개수 조회 (세션별)
    int countMessagesBySessionId(@Param("sessionId") String sessionId);

    // 이미지 관련 메소드
    int insertImage(ImageDTO image);
    List<ImageDTO> selectImagesByMessageId(@Param("messageId") int messageId);

    // 메시지와 이미지를 함께 조회
    List<MessageDTO> selectMessagesWithImagesBySessionId(@Param("sessionId") String sessionId);
    
    // 모든 세션과 첫 번째 메시지 요약 조회
    List<Map<String, Object>> selectAllSessionsWithFirstMessage();
}