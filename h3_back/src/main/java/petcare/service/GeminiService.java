package petcare.service;

import petcare.config.GeminiConfig;
import petcare.dto.GeminiRequestDTO;
import petcare.dto.GeminiResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class GeminiService {
    
    private final GeminiConfig config;
    private final RestTemplate geminiRestTemplate;
    
    /**
     * 텍스트만으로 질의
     */
    public GeminiResponseDTO generateText(String prompt) {
        GeminiRequestDTO request = GeminiRequestDTO.builder()
                .prompt(prompt)
                .build();
        
        return processGeminiRequest(request);
    }
    
    /**
     * 멀티모달 질의 (텍스트 + 이미지들)
     */
    public GeminiResponseDTO multimodalQuery(String prompt, List<MultipartFile> images) {
        GeminiRequestDTO request = GeminiRequestDTO.builder()
                .prompt(prompt)
                .images(images)
                .build();
        
        return processGeminiRequest(request);
    }
    
    /**
     * 펫 이미지 분석 전용
     */
    public GeminiResponseDTO analyzePetImages(List<MultipartFile> images, String analysisType) {
        String prompt = buildPetAnalysisPrompt(analysisType);
        
        return multimodalQuery(prompt, images);
    }
    
    /**
     * 구조화된 메시지로 질의 (system + user + images)
     */
    public GeminiResponseDTO structuredQuery(String systemMessage, String userMessage, List<MultipartFile> images) {
        GeminiRequestDTO request = GeminiRequestDTO.builder()
                .prompt(userMessage)
                .systemMessage(systemMessage)
                .images(images)
                .build();
        
        return processGeminiRequest(request);
    }
    
    /**
     * 구조화된 메시지로 질의 (system + user + images + config)
     */
    public GeminiResponseDTO structuredQueryWithConfig(String systemMessage, String userMessage, 
                                                     List<MultipartFile> images, Double temperature, Integer maxTokens) {
        GeminiRequestDTO request = GeminiRequestDTO.builder()
                .prompt(userMessage)
                .systemMessage(systemMessage)
                .images(images)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();
        
        return processGeminiRequest(request);
    }

    /**
     * 대화형 질의 (세션 유지)
     */
    public GeminiResponseDTO chatQuery(String prompt, String sessionId, List<MultipartFile> images) {
        GeminiRequestDTO request = GeminiRequestDTO.builder()
                .prompt(prompt)
                .images(images)
                .sessionId(sessionId)
                .build();
        
        return processGeminiRequest(request);
    }
    
    /**
     * GEMINI API 요청 처리 (핵심 메서드)
     */
    private GeminiResponseDTO processGeminiRequest(GeminiRequestDTO request) {
        try {
            // 1. HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (config.getKey() == null || config.getKey().isEmpty()) {
                throw new IllegalArgumentException("GEMINI API 키가 설정되지 않았습니다.");
            }
            headers.set("x-goog-api-key", config.getKey());
            
            // 2. 요청 본문 구성
            Map<String, Object> requestBody = buildRequestBody(request);
            
            // 3. HTTP 요청 엔티티 생성
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // 4. API 호출
            String url = config.getBaseUrl() + "/models/" + 
                        (request.getModel() != null ? request.getModel() : config.getModel()) + 
                        ":generateContent";
            
            ResponseEntity<Map> response = geminiRestTemplate.postForEntity(url, entity, Map.class);
            
            // 5. 응답 처리
            return parseGeminiResponse(response.getBody(), request);
            
        } catch (Exception e) {
            return GeminiResponseDTO.builder()
                    .success(false)
                    .error("GEMINI API 호출 중 오류 발생: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .imageCount(request.getImages() != null ? request.getImages().size() : 0)
                    .build();
        }
    }
    
    /**
     * GEMINI API 요청 본문 구성
     */
    private Map<String, Object> buildRequestBody(GeminiRequestDTO request) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        
        // System instruction 추가 (Gemini API 지원)
        if (request.getSystemMessage() != null && !request.getSystemMessage().isEmpty()) {
            Map<String, Object> systemInstruction = new HashMap<>();
            Map<String, Object> systemParts = new HashMap<>();
            systemParts.put("text", request.getSystemMessage());
            systemInstruction.put("parts", Arrays.asList(systemParts));
            requestBody.put("systemInstruction", systemInstruction);
        }
        
        // Contents 배열 생성
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        List<Map<String, Object>> parts = new ArrayList<>();
        
        // 텍스트 부분 추가
        if (request.getPrompt() != null && !request.getPrompt().isEmpty()) {
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", request.getPrompt());
            parts.add(textPart);
        }
        
        // 이미지 부분 추가 (멀티모달)
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (MultipartFile image : request.getImages()) {
                if (!image.isEmpty()) {
                    Map<String, Object> imagePart = new HashMap<>();
                    Map<String, Object> inlineData = new HashMap<>();
                    
                    inlineData.put("mime_type", image.getContentType());
                    inlineData.put("data", convertToBase64(image));
                    
                    imagePart.put("inline_data", inlineData);
                    parts.add(imagePart);
                }
            }
        }
        
        content.put("parts", parts);
        contents.add(content);
        requestBody.put("contents", contents);
        
        // Generation config 설정
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("maxOutputTokens", 
            request.getMaxTokens() != null ? request.getMaxTokens() : config.getMaxTokens());
        generationConfig.put("temperature", 
            request.getTemperature() != null ? request.getTemperature() : config.getTemperature());
        
        requestBody.put("generationConfig", generationConfig);
        
        return requestBody;
    }
    
    /**
     * GEMINI API 응답 파싱
     */
    private GeminiResponseDTO parseGeminiResponse(Map<String, Object> responseBody, GeminiRequestDTO request) {
        try {
            if (responseBody == null) {
                return GeminiResponseDTO.builder()
                        .success(false)
                        .error("응답이 없습니다.")
                        .timestamp(LocalDateTime.now())
                        .build();
            }
            
            // candidates에서 텍스트 추출
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return GeminiResponseDTO.builder()
                        .success(false)
                        .error("응답 후보가 없습니다.")
                        .timestamp(LocalDateTime.now())
                        .build();
            }
            
            Map<String, Object> firstCandidate = candidates.get(0);
            Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            
            String generatedText = "";
            if (parts != null && !parts.isEmpty()) {
                generatedText = (String) parts.get(0).get("text");
            }
            
            // 사용량 정보 추출 (있는 경우)
            Map<String, Object> usageMetadata = (Map<String, Object>) responseBody.get("usageMetadata");
            Integer tokensUsed = null;
            if (usageMetadata != null) {
                tokensUsed = (Integer) usageMetadata.get("totalTokenCount");
            }
            
            return GeminiResponseDTO.builder()
                    .success(true)
                    .content(generatedText)
                    .model(request.getModel() != null ? request.getModel() : config.getModel())
                    .tokensUsed(tokensUsed)
                    .sessionId(request.getSessionId())
                    .timestamp(LocalDateTime.now())
                    .imageCount(request.getImages() != null ? request.getImages().size() : 0)
                    .build();
                    
        } catch (Exception e) {
            return GeminiResponseDTO.builder()
                    .success(false)
                    .error("응답 파싱 중 오류 발생: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }
    
    /**
     * 이미지를 Base64로 변환
     */
    private String convertToBase64(MultipartFile image) throws IOException {
        byte[] imageBytes = image.getBytes();
        return Base64.getEncoder().encodeToString(imageBytes);
    }
    
    /**
     * 펫 분석용 프롬프트 생성
     */
    private String buildPetAnalysisPrompt(String analysisType) {
        switch (analysisType.toLowerCase()) {
            case "breed":
                return "이 펫의 품종을 식별해주세요. 가능한 한 구체적으로 설명해주세요.";
            case "health":
                return "이 펫의 건강 상태를 분석해주세요. 눈에 보이는 특징들을 바탕으로 건강 상태를 평가해주세요.";
            case "behavior":
                return "이 펫의 행동과 표정을 분석해서 현재 상태나 감정을 알려주세요.";
            case "general":
            default:
                return "이 펫에 대해 종합적으로 분석해주세요. 품종, 나이 추정, 건강 상태, 특징 등을 포함해서 설명해주세요.";
        }
    }

        
    /**
     * 구조화된 메시지로 질의 (system + user + imageUrls + config)
     */
    public GeminiResponseDTO structuredQueryWithConfigUrls(String systemMessage, String userMessage, 
                                                            List<String> imageUrls, Double temperature, Integer maxTokens) {
        GeminiRequestDTO request = GeminiRequestDTO.builder()
        .prompt(userMessage)
        .systemMessage(systemMessage)
        .imageUrls(imageUrls)
        .temperature(temperature)
        .maxTokens(maxTokens)
        .build();

        return processGeminiRequestWithUrls(request);
    }
    
    /**
     * URL 이미지를 포함한 GEMINI API 요청 처리
     */
    private GeminiResponseDTO processGeminiRequestWithUrls(GeminiRequestDTO request) {
    try {
        // 1. HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (config.getKey() == null || config.getKey().isEmpty()) {
            throw new IllegalArgumentException("GEMINI API 키가 설정되지 않았습니다.");
        }
        headers.set("x-goog-api-key", config.getKey());
        
        // 2. 요청 본문 구성 (URL 버전)
        Map<String, Object> requestBody = buildRequestBodyWithUrls(request);
        
        // 3. HTTP 요청 엔티티 생성
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        // 4. API 호출
        String url = config.getBaseUrl() + "/models/" + 
                    (request.getModel() != null ? request.getModel() : config.getModel()) + 
                    ":generateContent";
        
        ResponseEntity<Map> response = geminiRestTemplate.postForEntity(url, entity, Map.class);
        
        // 5. 응답 처리
        return parseGeminiResponse(response.getBody(), request);
        
    } catch (Exception e) {
        return GeminiResponseDTO.builder()
                .success(false)
                .error("GEMINI API 호출 중 오류 발생: " + e.getMessage())
                .timestamp(LocalDateTime.now())
                .imageCount(request.getImageUrls() != null ? request.getImageUrls().size() : 0)
                .build();
        }
    }

    /**
     * URL 이미지를 포함한 GEMINI API 요청 본문 구성
     */
    private Map<String, Object> buildRequestBodyWithUrls(GeminiRequestDTO request) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        
        // System instruction 추가 (Gemini API 지원)
        if (request.getSystemMessage() != null && !request.getSystemMessage().isEmpty()) {
            Map<String, Object> systemInstruction = new HashMap<>();
            Map<String, Object> systemParts = new HashMap<>();
            systemParts.put("text", request.getSystemMessage());
            systemInstruction.put("parts", Arrays.asList(systemParts));
            requestBody.put("systemInstruction", systemInstruction);
        }
        
        // Contents 배열 생성
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        List<Map<String, Object>> parts = new ArrayList<>();
        
        // 텍스트 부분 추가
        if (request.getPrompt() != null && !request.getPrompt().isEmpty()) {
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", request.getPrompt());
            parts.add(textPart);
        }
        
        // 이미지 URL 부분 추가 (멀티모달)
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            for (String imageUrl : request.getImageUrls()) {
                if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    try {
                        Map<String, Object> imagePart = new HashMap<>();
                        Map<String, Object> inlineData = new HashMap<>();
                        
                        // URL에서 이미지 다운로드 및 Base64 변환
                        byte[] imageBytes = downloadImageFromUrl(imageUrl);
                        String mimeType = detectMimeTypeFromUrl(imageUrl);
                        
                        inlineData.put("mime_type", mimeType);
                        inlineData.put("data", Base64.getEncoder().encodeToString(imageBytes));
                        
                        imagePart.put("inline_data", inlineData);
                        parts.add(imagePart);
                    } catch (Exception e) {
                        System.err.println("이미지 URL 처리 중 오류: " + imageUrl + " - " + e.getMessage());
                        // 개별 이미지 오류는 무시하고 계속 진행
                    }
                }
            }
        }
        
        content.put("parts", parts);
        contents.add(content);
        requestBody.put("contents", contents);
        
        // Generation config 설정
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("maxOutputTokens", 
            request.getMaxTokens() != null ? request.getMaxTokens() : config.getMaxTokens());
        generationConfig.put("temperature", 
            request.getTemperature() != null ? request.getTemperature() : config.getTemperature());
        
        requestBody.put("generationConfig", generationConfig);
        
        return requestBody;
    }

    /**
     * URL에서 이미지 다운로드
     */
    private byte[] downloadImageFromUrl(String imageUrl) throws IOException {
        try {
            ResponseEntity<byte[]> response = geminiRestTemplate.getForEntity(imageUrl, byte[].class);
            
            // 302 Found(리다이렉트)인 경우 Location 헤더에서 실제 URL을 가져와서 재시도
            if (response.getStatusCode() == HttpStatus.FOUND) {
                @SuppressWarnings("null")
                String redirectUrl = response.getHeaders().getLocation() != null ? 
                    response.getHeaders().getLocation().toString() : null;
                
                if (redirectUrl != null) {
                    System.out.println(">>>>> [GeminiService] 리다이렉트 감지: " + imageUrl + " -> " + redirectUrl);
                    // 리다이렉트된 URL로 다시 요청
                    ResponseEntity<byte[]> redirectResponse = geminiRestTemplate.getForEntity(redirectUrl, byte[].class);
                    if (redirectResponse.getStatusCode().is2xxSuccessful() && redirectResponse.getBody() != null) {
                        return redirectResponse.getBody();
                    } else {
                        throw new IOException("리다이렉트된 이미지 다운로드 실패: HTTP " + redirectResponse.getStatusCode());
                    }
                } else {
                    throw new IOException("리다이렉트 Location 헤더가 없습니다");
                }
            }
            // 일반적인 2xx 성공 응답
            else if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } 
            // 기타 오류 응답
            else {
                throw new IOException("이미지 다운로드 실패: HTTP " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new IOException("이미지 다운로드 중 오류: " + e.getMessage(), e);
        }
    }

    /**
     * URL에서 MIME 타입 추측
     */
    private String detectMimeTypeFromUrl(String imageUrl) {
        String lowerUrl = imageUrl.toLowerCase();
        if (lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerUrl.endsWith(".png")) {
            return "image/png";
        } else if (lowerUrl.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerUrl.endsWith(".webp")) {
            return "image/webp";
        } else {
            return "image/jpeg"; // 기본값
        }
    }
    
}