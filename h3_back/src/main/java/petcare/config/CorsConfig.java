package petcare.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {
    
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${cors.allowed-origin-patterns}")
    private String allowedOriginPatterns;
    
    @Value("${cors.allowed-methods}")
    private String allowedMethods;
    
    @Value("${cors.allowed-headers}")
    private String allowedHeaders;
    
    @Value("${cors.max-age}")
    private Long maxAge;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // origins 허용
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        for (String origin : origins) {
            configuration.addAllowedOrigin(origin.trim());
        }
        // originPatterns 허용
        List<String> originPatterns = Arrays.asList(allowedOriginPatterns.split(","));
        for (String originPattern : originPatterns) {
            configuration.addAllowedOriginPattern(originPattern.trim());
        }
        
        // 콤마로 구분된 methods를 리스트로 변환
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        for (String method : methods) {
            configuration.addAllowedMethod(method.trim());
        }
        
        // 허용할 헤더 (*는 모든 헤더 허용)
        if ("*".equals(allowedHeaders.trim())) {
            configuration.addAllowedHeader("*");
        } else {
            List<String> headers = Arrays.asList(allowedHeaders.split(","));
            for (String header : headers) {
                configuration.addAllowedHeader(header.trim());
            }
        }
        
        // 쿠키/인증 정보 포함 허용
        configuration.setAllowCredentials(true);
        
        // preflight 캐시 시간 (properties에서 설정)
        configuration.setMaxAge(maxAge);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}