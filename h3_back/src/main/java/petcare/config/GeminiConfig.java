package petcare.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Gemini API 설정 클래스
 * application.properties에 설정된 값을 읽어와서 사용
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "gemini.api")
public class GeminiConfig {
    private String key;
    private String baseUrl;
    private String model;
    private int timeout;
    private int maxTokens;
    private double temperature;
    
    @Bean
    public RestTemplate geminiRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        // 타임아웃 설정 등 필요시 추가
        return restTemplate;
    }
} 