package petcare.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import petcare.mapper.AiVetMapper;
import petcare.dto.AiVetDTO;
import petcare.dto.ImageDTO;
import petcare.dto.MessageDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import petcare.service.ImageUploadService;



@RestController
@RequestMapping("/aivet")
public class AiVetController {
    
    @Value("${fastapi.aivet.server}")
    private String fastapi_url;

    @PostMapping("/diagnose")
    public Map<String, Object> diagnoseSkin(
        
        @RequestParam(value = "images", required = false) List<MultipartFile> images,
        @RequestParam("text") String text,
        @RequestParam(value = "sessionId", required = false) String sessionId,
        @RequestParam(value = "mode", required = false, defaultValue = "default") String mode // 추가!
    ) {
        System.out.println("🚀🚀🚀 API 호출됨!!! 🚀🚀🚀");
        // 🔍 디버깅 로그 추가
        System.out.println("=== API 호출 디버깅 ===");
        System.out.println("📝 받은 text: [" + text + "]");
        System.out.println("📸 받은 images: " + (images != null ? images.size() + "개" : "null"));
        System.out.println("🆔 받은 sessionId: [" + sessionId + "]");
        System.out.println("모드: [" + mode + "]"); // 모드 디버깅 로그 추가
        System.out.println("========================");
            
            
        
        try {
            // 1. 세션 ID 처리 (없으면 새로 생성)
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = UUID.randomUUID().toString();
                
                // 새 세션 생성
                AiVetDTO session = new AiVetDTO();
                session.setSessionId(sessionId);
                session.setUserId("anonymous");
                session.setSummary("AI 피부진단 대화");
                session.setStatus("active");
                aiVetMapper.insertSession(session);
            }
            
            // 2. 사용자 메시지 저장
            MessageDTO userMessage = new MessageDTO();
            userMessage.setSessionId(sessionId);
            userMessage.setSenderType("user");
            // 📌 null 체크 및 디버깅 추가
            System.out.println("🔍 받은 text 파라미터: [" + text + "]");
            String content = (text != null && !text.trim().isEmpty()) ? text.trim() : "텍스트 없음";
            userMessage.setContent(content);
            System.out.println("🔍 DB에 저장할 content: [" + userMessage.getContent() + "]");
            aiVetMapper.insertMessage(userMessage);
            
            // 🆕 2-1. 이미지 저장 (메시지 저장 직후)
            if (images != null && !images.isEmpty()) {
                int messageId = userMessage.getMessageId();  // useGeneratedKeys로 자동 생성된 ID
                List<String> savedImagePaths = imageUploadService.saveImages(images, messageId, sessionId);
                System.out.println("💾 저장된 이미지: " + savedImagePaths.size() + "개");
                System.out.println("컨트롤러 images: " + (images != null ? images.size() : "null"));
            }

            // 3-1. 이전 대화 조회 (이미지 포함)
            List<MessageDTO> previousMessages = aiVetMapper.selectMessagesWithImagesBySessionId(sessionId);            
            
            //이전 이력 확인하는지 디버깅
            System.out.println("🔍 이전 메시지 개수: " + previousMessages.size());
            for (MessageDTO msg : previousMessages) {
                System.out.println("  - " + msg.getSenderType() + ": " + msg.getContent());
                if (msg.getImages() != null && !msg.getImages().isEmpty()) {
                    System.out.println("    📸 이미지: " + msg.getImages().size() + "개");
                    for (ImageDTO img : msg.getImages()) {
                        System.out.println("      경로: " + img.getImagePath());
                    }
                }
            }
            // 3-2. Python AI 서버 호출 (이전 대화 포함)
            RestTemplate restTemplate = new RestTemplate();
            String pythonUrl = fastapi_url + "/api/diagnose-skin";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("text", content);

            // 이미지 저장은 위에서 이미 했으므로, DB에서 저장된 경로를 조회해서 Python에 전송
            if (images != null && !images.isEmpty()) {
                int messageId = userMessage.getMessageId();
                List<ImageDTO> savedImages = aiVetMapper.selectImagesByMessageId(messageId);
                
                // 저장된 파일 경로들을 Python에 전송
                for (ImageDTO img : savedImages) {
                    body.add("image_paths", img.getImagePath());
                }
                System.out.println("📂 Python에 이미지 경로 전송: " + savedImages.size() + "개");
            } else {
                System.out.println("📝 텍스트만으로 Python 서버 호출");
            }

            // 이전 대화 JSON으로 변환해서 전달
            //ObjectMapper objectMapper = new ObjectMapper();
            String conversationHistory = objectMapper.writeValueAsString(previousMessages);
            body.add("conversation_history", conversationHistory);
            // mode 파라미터 Python 서버로 전달
            body.add("mode", mode);

            // Python 서버 호출 후 응답을 받는 부분
            HttpEntity<MultiValueMap<String, Object>> requestEntity = 
                new HttpEntity<>(body, headers);
            
            // Python 서버 호출
            String aiDiagnosis = "AI 진단 처리 중입니다...";  // 기본값
            try {
                ResponseEntity<Map> response = restTemplate.postForEntity(
                    pythonUrl, requestEntity, Map.class);
                System.out.println("✅ Python 응답 받음: " + response.getBody());
                Object diagnosisObj = response.getBody().get("diagnosis");
                if (diagnosisObj instanceof String) {
                    aiDiagnosis = (String) diagnosisObj;
                    // 하이브리드 진단 결과가 JSON string이면 사람이 읽기 쉬운 텍스트로 변환
                    if (aiDiagnosis.trim().startsWith("{") && aiDiagnosis.contains("hybrid")) {
                        try {
                            Map<String, Object> diagnosisMap = objectMapper.readValue(aiDiagnosis, Map.class);
                            if (diagnosisMap.containsKey("hybrid") && (Boolean.TRUE.equals(diagnosisMap.get("hybrid")))) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("[하이브리드 진단 모드: ON]\n\n");
                                // EfficientNet 결과
                                sb.append("[EfficientNet 예측 결과]\n");
                                Object effResults = diagnosisMap.get("efficientnet_results");
                                if (effResults instanceof java.util.List && !((java.util.List)effResults).isEmpty()) {
                                    int idx = 1;
                                    for (Object r : (java.util.List)effResults) {
                                        Map rMap = (Map) r;
                                        sb.append("  - 이미지 ").append(idx++).append(": class=").append(rMap.get("class"));
                                        sb.append(", confidence=").append(rMap.get("confidence"));
                                        sb.append(", probs=").append(rMap.get("probabilities")).append("\n");
                                    }
                                } else {
                                    sb.append("  (결과 없음)\n");
                                }
                                // Gemini 결과
                                sb.append("\n[Gemini 진단 결과]\n");
                                Object gemini = diagnosisMap.get("gemini_result");
                                if (gemini instanceof Map) {
                                    Map geminiMap = (Map) gemini;
                                    if (geminiMap.containsKey("final_response")) {
                                        sb.append(geminiMap.get("final_response"));
                                    } else if (geminiMap.containsKey("error")) {
                                        sb.append("Gemini 진단 실패: ").append(geminiMap.get("error"));
                                        if (geminiMap.containsKey("raw")) {
                                            sb.append("\n상세: ").append(geminiMap.get("raw"));
                                        }
                                    } else {
                                        sb.append(geminiMap.toString());
                                    }
                                } else {
                                    sb.append(gemini);
                                }
                                aiDiagnosis = sb.toString();
                            }
                        } catch (Exception parseEx) {
                            // 파싱 실패시 원본 string 그대로 사용
                        }
                    }
                } else {
                    // diagnosis가 Map 등일 경우: 하이브리드라면 사람이 읽기 쉬운 텍스트로 변환
                    if (diagnosisObj instanceof Map) {
                        Map diagnosisMap = (Map) diagnosisObj;
                        if (diagnosisMap.containsKey("hybrid") && Boolean.TRUE.equals(diagnosisMap.get("hybrid"))) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("[하이브리드 진단 모드: ON]\n\n");
                            // EfficientNet 결과
                            sb.append("[EfficientNet 예측 결과]\n");
                            Object effResults = diagnosisMap.get("efficientnet_results");
                            if (effResults instanceof java.util.List && !((java.util.List)effResults).isEmpty()) {
                                int idx = 1;
                                for (Object r : (java.util.List)effResults) {
                                    Map rMap = (Map) r;
                                    sb.append("  - 이미지 ").append(idx++).append(": class=").append(rMap.get("class"));
                                    sb.append(", confidence=").append(rMap.get("confidence"));
                                    sb.append(", probs=").append(rMap.get("probabilities")).append("\n");
                                }
                            } else {
                                sb.append("  (결과 없음)\n");
                            }
                            // Gemini 결과
                            sb.append("\n[Gemini 진단 결과]\n");
                            Object gemini = diagnosisMap.get("gemini_result");
                            if (gemini instanceof Map) {
                                Map geminiMap = (Map) gemini;
                                if (geminiMap.containsKey("final_response")) {
                                    sb.append(geminiMap.get("final_response"));
                                } else if (geminiMap.containsKey("error")) {
                                    sb.append("Gemini 진단 실패: ").append(geminiMap.get("error"));
                                    if (geminiMap.containsKey("raw")) {
                                        sb.append("\n상세: ").append(geminiMap.get("raw"));
                                    }
                                } else {
                                    sb.append(geminiMap.toString());
                                }
                            } else {
                                sb.append(gemini);
                            }
                            aiDiagnosis = sb.toString();
                        } else {
                            // 하이브리드가 아니면 그냥 JSON 문자열로
                            aiDiagnosis = objectMapper.writeValueAsString(diagnosisObj);
                        }
                    } else {
                        aiDiagnosis = objectMapper.writeValueAsString(diagnosisObj);
                    }
                }
                System.out.println("✅ 진단 결과 추출: " + aiDiagnosis);
                
            } catch (Exception e) {
                System.out.println("❌ Python 통신 에러: " + e.getMessage());
                aiDiagnosis = "AI 서버 연결 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
                e.printStackTrace();
            }
            // 

            // 4. AI 응답 메시지 저장
            MessageDTO aiMessage = new MessageDTO();
            aiMessage.setSessionId(sessionId);
            aiMessage.setSenderType("ai");
            aiMessage.setContent(aiDiagnosis);
            aiVetMapper.insertMessage(aiMessage);
            
            // 5. 응답 반환
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("diagnosis", aiDiagnosis);
            result.put("sessionId", sessionId);
            return result;
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return error;
        }
    }

    // DB 테스트용 - 의존성 주입
    @Autowired
    private AiVetMapper aiVetMapper;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ImageUploadService imageUploadService;    

    // DB 테스트 1: 세션 생성
    @PostMapping("/test-db-insert")
    public Map<String, Object> testDbInsert() {
        try {
            AiVetDTO session = new AiVetDTO();
            session.setSessionId(UUID.randomUUID().toString());
            session.setUserId("test_user");
            session.setSummary("DB 연결 테스트 세션");
            session.setStatus("active");
            
            int result = aiVetMapper.insertSession(session);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("insertResult", result);
            response.put("sessionId", session.getSessionId());
            return response;
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return error;
        }
    }

    // DB 테스트 2: 최근 세션 조회
    @GetMapping("/test-db-select")
    public Map<String, Object> testDbSelect() {
        try {
            List<AiVetDTO> sessions = aiVetMapper.selectRecentSessions();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("sessions", sessions);
            response.put("count", sessions.size());
            return response;
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return error;
        }
    }
    // AiVetController.java에 추가할 메소드들

    // 1. 메시지 조회만 (이미지도 같이 조회)  
    @GetMapping("/session/{sessionId}/messages")
    public Map<String, Object> getSessionMessages(@PathVariable String sessionId) {
        try {
            List<MessageDTO> messages = aiVetMapper.selectMessagesWithImagesBySessionId(sessionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("messages", messages);
            response.put("sessionId", sessionId);
            response.put("messageCount", messages.size());
            return response;
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return error;
        }
    }

    // 2. 새 세션 생성만
    @PostMapping("/session/new")
    public Map<String, Object> createNewSession() {
        try {
            String newSessionId = UUID.randomUUID().toString();
            AiVetDTO newSession = new AiVetDTO();
            newSession.setSessionId(newSessionId);
            newSession.setUserId("anonymous");
            newSession.setSummary("AI 피부진단 대화");
            newSession.setStatus("active");
            
            aiVetMapper.insertSession(newSession);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("newSessionId", newSessionId);
            return response;
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return error;
        }
    }
    // 모든 세션 목록 조회 (최신순, 첫 메시지 요약 포함)
    @GetMapping("/sessions/all")
    public Map<String, Object> getAllSessions() {
        try {
            List<Map<String, Object>> sessionSummaries = aiVetMapper.selectAllSessionsWithFirstMessage();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("sessions", sessionSummaries);
            response.put("count", sessionSummaries.size());
            return response;
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return error;
        }
    }

}