package petcare.dto;

import lombok.Data;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
@Builder
public class GeminiRequestDTO {
    private String prompt;                    // 텍스트 프롬프트
    private String systemMessage;             // 시스템 메시지 (선택적)
    private String model;                     // 사용할 모델 (선택적)
    private Integer maxTokens;                // 최대 토큰 수 (선택적)
    private Double temperature;               // 창의성 수준 (선택적)
    private List<MultipartFile> images;       // 이미지 파일들
    private List<String> imageUrls;           // 이미지 URL들 (추가)
    private String sessionId;                 // 대화 세션 ID (선택적)
    private String context;                   // 추가 컨텍스트 (선택적)
} 