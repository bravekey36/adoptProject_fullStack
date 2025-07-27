package petcare.dto;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Builder
public class GeminiResponseDTO {
    private boolean success;                  // 성공 여부
    private String content;                   // 생성된 텍스트
    private String model;                     // 사용된 모델
    private Integer tokensUsed;               // 사용된 토큰 수
    private String sessionId;                 // 세션 ID
    private String error;                     // 오류 메시지
    private LocalDateTime timestamp;          // 응답 시간
    private Integer imageCount;               // 처리된 이미지 수
} 