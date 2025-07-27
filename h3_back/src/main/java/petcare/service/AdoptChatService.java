package petcare.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import petcare.dto.AdoptChatSessionDTO;
import petcare.dto.AdoptChatMessageDTO;
import petcare.dto.AdoptDTO;
import petcare.mapper.AdoptChatMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdoptChatService {

    @Autowired
    private AdoptChatMapper adoptChatMapper;

    @Autowired
    private ObjectMapper objectMapper;

    // ============ 세션 관리 ============

    /**
     * 새 세션 생성
     */
    @Transactional
    public AdoptChatSessionDTO createNewSession(String userId, String initialMessage) {
        String sessionId = UUID.randomUUID().toString();
        
        // 세션 제목 생성 (초기 메시지의 첫 50자)
        String sessionTitle = generateSessionTitle(initialMessage);
        
        AdoptChatSessionDTO session = new AdoptChatSessionDTO(sessionId, userId, sessionTitle);
        
        int result = adoptChatMapper.insertSession(session);
        if (result > 0) {
            return adoptChatMapper.selectSessionById(sessionId);
        }
        
        throw new RuntimeException("세션 생성에 실패했습니다.");
    }

    /**
     * 사용자별 세션 목록 조회
     */
    public List<AdoptChatSessionDTO> getUserSessions(String userId) {
        return adoptChatMapper.selectSessionsByUserId(userId);
    }

    /**
     * 세션 조회 (권한 체크 포함)
     */
    public AdoptChatSessionDTO getSession(String sessionId, String userId) {
        AdoptChatSessionDTO session = adoptChatMapper.selectSessionById(sessionId);
        
        if (session == null) {
            throw new RuntimeException("세션을 찾을 수 없습니다.");
        }
        
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("세션에 접근할 권한이 없습니다.");
        }
        
        return session;
    }

    /**
     * 세션 상태 업데이트
     */
    @Transactional
    public void updateSessionStatus(String sessionId, String userId, String status) {
        // 권한 체크
        getSession(sessionId, userId);
        
        adoptChatMapper.updateSessionStatus(sessionId, status);
    }

    /**
     * 세션 제목 업데이트
     */
    @Transactional
    public void updateSessionTitle(String sessionId, String userId, String title) {
        // 권한 체크
        getSession(sessionId, userId);
        
        adoptChatMapper.updateSessionTitle(sessionId, title);
    }

    // ============ 메시지 관리 ============

    /**
     * 사용자 메시지 저장
     */
    @Transactional
    public AdoptChatMessageDTO saveUserMessage(String sessionId, String userId, String content) {
        // 권한 체크
        getSession(sessionId, userId);
        
        // 다음 sequence 번호 조회
        Integer nextSequence = adoptChatMapper.selectNextSequence(sessionId);
        
        AdoptChatMessageDTO message = new AdoptChatMessageDTO(sessionId, "USER", content, nextSequence);
        
        int result = adoptChatMapper.insertMessage(message);
        if (result > 0) {
            // 세션 업데이트 시간 갱신
            adoptChatMapper.updateSessionTimestamp(sessionId);
            
            // 첫 번째 메시지인 경우 세션 제목 업데이트
            int messageCount = adoptChatMapper.countMessagesBySessionId(sessionId);
            if (messageCount == 1) {
                String sessionTitle = generateSessionTitle(content);
                adoptChatMapper.updateSessionTitle(sessionId, sessionTitle);
            }
            
            return message;
        }
        
        throw new RuntimeException("메시지 저장에 실패했습니다.");
    }

    /**
     * AI 응답 메시지 저장
     */
    @Transactional
    public AdoptChatMessageDTO saveBotMessage(String sessionId, String userId, String content, List<AdoptDTO> pets) {
        // 권한 체크
        getSession(sessionId, userId);
        
        // 다음 sequence 번호 조회
        Integer nextSequence = adoptChatMapper.selectNextSequence(sessionId);
        
        // 보호동물 정보를 JSON으로 변환
        String petsJson = null;
        if (pets != null && !pets.isEmpty()) {
            try {
                petsJson = objectMapper.writeValueAsString(pets);
            } catch (JsonProcessingException e) {
                // JSON 변환 실패 시 로그 남기고 계속 진행
                System.err.println("보호동물 정보 JSON 변환 실패: " + e.getMessage());
            }
        }
        
        AdoptChatMessageDTO message = new AdoptChatMessageDTO(sessionId, "BOT", content, petsJson, nextSequence);
        
        int result = adoptChatMapper.insertMessage(message);
        if (result > 0) {
            // 세션 업데이트 시간 갱신
            adoptChatMapper.updateSessionTimestamp(sessionId);
            return message;
        }
        
        throw new RuntimeException("AI 응답 저장에 실패했습니다.");
    }

    /**
     * 세션의 모든 메시지 조회
     */
    public List<AdoptChatMessageDTO> getSessionMessages(String sessionId, String userId) {
        // 권한 체크
        getSession(sessionId, userId);
        
        return adoptChatMapper.selectMessagesBySessionId(sessionId);
    }

    /**
     * 세션의 대화 히스토리를 LLM용 포맷으로 변환 (검색 조건 포함)
     */
    public String getSessionHistoryForLLM(String sessionId, String userId) {
        List<AdoptChatMessageDTO> messages = getSessionMessages(sessionId, userId);
        
        System.out.println("[AdoptChatService] LLM용 히스토리 생성 - 메시지 개수: " + messages.size());
        
        StringBuilder history = new StringBuilder();
        
        // 대화가 많으면 요약 제공
        if (messages.size() > 6) {
            history.append("=== 대화 요약 ===\n");
            history.append(generateConversationSummary(messages));
            history.append("\n=== 최근 대화 (최신 3턴) ===\n");
            // 최근 6개 메시지만 포함 (사용자 3턴 + AI 3턴)
            messages = messages.subList(Math.max(0, messages.size() - 6), messages.size());
        } else {
            history.append("=== 대화 내역 ===\n");
        }
        
        String lastSearchConditions = "";
        
        for (int i = 0; i < messages.size(); i++) {
            AdoptChatMessageDTO message = messages.get(i);
            if ("USER".equals(message.getMessageType())) {
                history.append(String.format("[%d] 사용자: %s\n", i + 1, message.getContent()));
            } else if ("BOT".equals(message.getMessageType())) {
                // AI 응답에서 추천 동물 정보 제외하고 텍스트만 추출
                String content = message.getContent();
                if (content.length() > 200) {
                    content = content.substring(0, 200) + "...";
                }
                history.append(String.format("[%d] AI: %s\n", i + 1, content));
                
                // 보호동물 추천이 있었다면 마지막 검색 조건으로 간주
                if (message.getPetsJson() != null && !message.getPetsJson().isEmpty()) {
                    lastSearchConditions = extractSearchConditionsFromPets(message.getPetsJson());
                }
            }
        }
        
        if (!lastSearchConditions.isEmpty()) {
            history.append("\n[마지막 검색 조건]\n").append(lastSearchConditions).append("\n");
        }
        
        history.append("\n=== 현재 요청 ===\n");
        history.append("위의 대화 맥락을 고려하여 사용자의 새로운 요청에 답변해주세요.\n");
        
        String historyString = history.toString();
        System.out.println("[AdoptChatService] 생성된 히스토리:");
        System.out.println("=====================================");
        System.out.println(historyString);
        System.out.println("=====================================");
        
        return historyString;
    }
    
    /**
     * 보호동물 JSON에서 검색 조건 추출
     */
    private String extractSearchConditionsFromPets(String petsJson) {
        try {
            List<Map<String, Object>> pets = objectMapper.readValue(petsJson, new TypeReference<List<Map<String, Object>>>() {});
            if (pets.isEmpty()) return "";
            
            Map<String, Object> firstPet = pets.get(0);
            StringBuilder conditions = new StringBuilder();
            
            if (firstPet.get("breed_cd") != null) {
                conditions.append("품종: ").append(firstPet.get("breed_cd")).append(" ");
            }
            if (firstPet.get("gender_cd") != null) {
                conditions.append("성별: ").append(firstPet.get("gender_cd")).append(" ");
            }
            if (firstPet.get("weight_kg") != null) {
                Double weight = (Double) firstPet.get("weight_kg");
                if (weight <= 10) {
                    conditions.append("크기: 소형견 ");
                } else if (weight <= 20) {
                    conditions.append("크기: 중형견 ");
                } else {
                    conditions.append("크기: 대형견 ");
                }
            }
            if (firstPet.get("color") != null) {
                conditions.append("털색: ").append(firstPet.get("color")).append(" ");
            }
            
            return conditions.toString().trim();
        } catch (Exception e) {
            System.err.println("검색 조건 추출 실패: " + e.getMessage());
            return "";
        }
    }

    // ============ 유틸리티 메서드 ============

    /**
     * 세션 제목 생성 (첫 메시지에서 50자 추출)
     */
    private String generateSessionTitle(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "새로운 상담";
        }
        
        String title = content.trim();
        if (title.length() > 50) {
            title = title.substring(0, 47) + "...";
        }
        
        return title;
    }

    /**
     * 대화 요약 생성
     */
    private String generateConversationSummary(List<AdoptChatMessageDTO> messages) {
        StringBuilder summary = new StringBuilder();
        
        String userPreferences = "";
        boolean hasRecommendations = false;
        
        for (AdoptChatMessageDTO message : messages) {
            if ("USER".equals(message.getMessageType())) {
                String content = message.getContent().toLowerCase();
                
                // 사용자 선호도 추출
                if (content.contains("대형") || content.contains("큰")) {
                    userPreferences += "대형견 선호, ";
                }
                if (content.contains("소형") || content.contains("작은")) {
                    userPreferences += "소형견 선호, ";
                }
                if (content.contains("중형")) {
                    userPreferences += "중형견 선호, ";
                }
                if (content.contains("흰색") || content.contains("화이트")) {
                    userPreferences += "흰색 털 선호, ";
                }
                if (content.contains("갈색") || content.contains("브라운")) {
                    userPreferences += "갈색 털 선호, ";
                }
                if (content.contains("검정") || content.contains("블랙")) {
                    userPreferences += "검정 털 선호, ";
                }
                if (content.contains("온순") || content.contains("얌전")) {
                    userPreferences += "온순한 성격 선호, ";
                }
                if (content.contains("활발") || content.contains("활동적")) {
                    userPreferences += "활발한 성격 선호, ";
                }
            } else if ("BOT".equals(message.getMessageType()) && 
                      message.getPetsJson() != null && !message.getPetsJson().isEmpty()) {
                hasRecommendations = true;
            }
        }
        
        if (!userPreferences.isEmpty()) {
            summary.append("사용자 선호도: ").append(userPreferences.replaceAll(", $", "")).append("\n");
        }
        
        if (hasRecommendations) {
            summary.append("이전에 동물 추천을 제공했음\n");
        }
        
        return summary.toString().isEmpty() ? "특별한 선호도 없음\n" : summary.toString();
    }

    /**
     * 만료된 세션 정리 (스케줄러에서 호출)
     */
    @Transactional
    public int cleanupExpiredSessions() {
        return adoptChatMapper.deleteExpiredSessions();
    }

    /**
     * 세션 통계 조회
     */
    public int getMessageCount(String sessionId, String userId) {
        // 권한 체크
        getSession(sessionId, userId);
        
        return adoptChatMapper.countMessagesBySessionId(sessionId);
    }
}
