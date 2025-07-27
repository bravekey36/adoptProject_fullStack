package petcare.controller;

import petcare.dto.GeminiResponseDTO;
import petcare.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gemini")
@RequiredArgsConstructor
public class GeminiController {
    
    private final GeminiService geminiService;
    
    /**
     * 텍스트 생성
     */
    @PostMapping("/generate-text")
    public ResponseEntity<GeminiResponseDTO> generateText(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        
        if (prompt == null || prompt.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                GeminiResponseDTO.builder()
                    .success(false)
                    .error("프롬프트가 필요합니다.")
                    .build()
            );
        }
        
        GeminiResponseDTO response = geminiService.generateText(prompt);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 멀티모달 질의 (텍스트 + 이미지들) - 핵심 기능!
     */
    @PostMapping("/multimodal-query")
    public ResponseEntity<GeminiResponseDTO> multimodalQuery(
            @RequestParam("prompt") String prompt,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        
        if ((prompt == null || prompt.trim().isEmpty()) && 
            (images == null || images.isEmpty())) {
            return ResponseEntity.badRequest().body(
                GeminiResponseDTO.builder()
                    .success(false)
                    .error("프롬프트 또는 이미지가 필요합니다.")
                    .build()
            );
        }
        
        GeminiResponseDTO response = geminiService.multimodalQuery(prompt, images);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 펫 이미지 분석
     */
    @PostMapping("/analyze-pet")
    public ResponseEntity<GeminiResponseDTO> analyzePet(
            @RequestParam("images") List<MultipartFile> images,
            @RequestParam(value = "analysisType", defaultValue = "general") String analysisType) {
        
        if (images == null || images.isEmpty()) {
            return ResponseEntity.badRequest().body(
                GeminiResponseDTO.builder()
                    .success(false)
                    .error("분석할 이미지가 필요합니다.")
                    .build()
            );
        }
        
        GeminiResponseDTO response = geminiService.analyzePetImages(images, analysisType);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 대화형 채팅
     */
    @PostMapping("/chat")
    public ResponseEntity<GeminiResponseDTO> chat(
            @RequestParam("prompt") String prompt,
            @RequestParam(value = "sessionId", required = false) String sessionId,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        
        GeminiResponseDTO response = geminiService.chatQuery(prompt, sessionId, images);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 서비스 상태 확인
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "service", "GEMINI API Service",
            "timestamp", System.currentTimeMillis()
        ));
    }
} 