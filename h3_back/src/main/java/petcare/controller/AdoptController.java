package petcare.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.Authentication;

import petcare.dto.AdoptChatSessionDTO;
import petcare.dto.AdoptChatMessageDTO;
import petcare.service.AdoptChatService;
import petcare.service.UserService;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/adopt")
public class AdoptController {
    private static final Logger logger = LoggerFactory.getLogger(AdoptController.class);

    @Value("${fastapi.adopt.server}")
    private String fastApiUrl;

    @Autowired
    private petcare.service.AdoptService adoptService;

    @Autowired
    private AdoptChatService adoptChatService;

    @Autowired
    private UserService userService;

    // AI 처리 (단순 버전)
    @PostMapping("/AIprocess")
    public ResponseEntity<Map<String, Object>> processWithAI(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String prompt = (String) request.get("prompt");
            if (prompt == null || prompt.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "프롬프트가 비어있습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // FastAPI 호출
            Map<String, Object> llmResult = callFastAPI(prompt, null);
            
            // LLM 결과 처리
            @SuppressWarnings("unchecked")
            Map<String, Object> query = (Map<String, Object>) llmResult.get("query");
            String answer = (String) llmResult.get("answer");

            List<petcare.dto.AdoptDTO> pets = null;
            if (query != null) {
                pets = adoptService.findPetsByQuery(query);
            }

            response.put("success", true);
            response.put("answer", answer);
            response.put("pets", pets);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("[AdoptController] AI 처리 오류", e);
            response.put("success", false);
            response.put("error", "서버 오류: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // FastAPI 호출 공통 메서드
    private Map<String, Object> callFastAPI(String prompt, String history) throws Exception {
        Map<String, String> fastApiRequest = new HashMap<>();
        fastApiRequest.put("prompt", prompt);
        if (history != null) {
            fastApiRequest.put("history", history);
        }

        RestTemplate restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
        
        String apiUrl = fastApiUrl + "/api/llm-query-adopt";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(fastApiRequest, headers);

        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> fastApiResponse = restTemplate.postForEntity(apiUrl, entity, Map.class);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> llmResult = (Map<String, Object>) fastApiResponse.getBody();
        
        if (llmResult == null) {
            throw new RuntimeException("LLM 서버 응답이 없습니다.");
        }
        
        return llmResult;
    }

    // 사용자 정보 추출 공통 메서드
    private String getUserIdFromAuth(Authentication authentication) throws Exception {
        String username = authentication.getName();
        return userService.getUserIdFromUsername(username);
    }

    // ============ 세션 관리 API ============

    /**
     * 새 세션 생성
     */
    @PostMapping("/sessions")
    public ResponseEntity<Map<String, Object>> createSession(
            Authentication authentication,
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String userId = getUserIdFromAuth(authentication);
            String initialMessage = request.getOrDefault("initialMessage", "새로운 상담을 시작합니다.");
            
            AdoptChatSessionDTO session = adoptChatService.createNewSession(userId, initialMessage);
            
            response.put("success", true);
            response.put("session", session);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("[AdoptController] 세션 생성 오류", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 사용자별 세션 목록 조회
     */
    @GetMapping("/sessions")
    public ResponseEntity<Map<String, Object>> getSessions(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String userId = getUserIdFromAuth(authentication);
            List<AdoptChatSessionDTO> sessions = adoptChatService.getUserSessions(userId);
            
            response.put("success", true);
            response.put("sessions", sessions);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("[AdoptController] 세션 목록 조회 오류", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 세션의 대화 내역 조회
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<Map<String, Object>> getSessionMessages(
            @PathVariable String sessionId,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String userId = getUserIdFromAuth(authentication);
            List<AdoptChatMessageDTO> messages = adoptChatService.getSessionMessages(sessionId, userId);
            
            response.put("success", true);
            response.put("messages", messages);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("[AdoptController] 세션 메시지 조회 오류", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 세션별 AI 처리
     */
    @PostMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<Map<String, Object>> processWithAIInSession(
            @PathVariable String sessionId,
            Authentication authentication,
            @RequestBody Map<String, Object> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String userId = getUserIdFromAuth(authentication);
            String prompt = (String) request.get("prompt");
            
            if (prompt == null || prompt.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "프롬프트가 비어있습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 1. 사용자 메시지 저장
            adoptChatService.saveUserMessage(sessionId, userId, prompt);
            
            // 2. 세션 히스토리 포함하여 AI 처리
            String sessionHistory = adoptChatService.getSessionHistoryForLLM(sessionId, userId);
            Map<String, Object> llmResult = callFastAPI(prompt, sessionHistory);
            
            // 3. LLM 결과 처리
            @SuppressWarnings("unchecked")
            Map<String, Object> query = (Map<String, Object>) llmResult.get("query");
            String answer = (String) llmResult.get("answer");

            List<petcare.dto.AdoptDTO> pets = null;
            if (query != null) {
                pets = adoptService.findPetsByQuery(query);
            }

            // 4. AI 응답 저장
            adoptChatService.saveBotMessage(sessionId, userId, answer, pets);

            response.put("success", true);
            response.put("answer", answer);
            response.put("pets", pets);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("[AdoptController] 세션 AI 처리 오류", e);
            response.put("success", false);
            response.put("error", "서버 오류: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 세션 상태 업데이트
     */
    @PutMapping("/sessions/{sessionId}/status")
    public ResponseEntity<Map<String, Object>> updateSessionStatus(
            @PathVariable String sessionId,
            Authentication authentication,
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String userId = getUserIdFromAuth(authentication);
            String status = request.get("status");
            
            adoptChatService.updateSessionStatus(sessionId, userId, status);
            
            response.put("success", true);
            response.put("message", "세션 상태가 업데이트되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("[AdoptController] 세션 상태 업데이트 오류", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}