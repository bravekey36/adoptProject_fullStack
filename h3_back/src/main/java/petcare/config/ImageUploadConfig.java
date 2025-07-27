package petcare.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "file.upload")
public class ImageUploadConfig {
    
    private String basePath = "chatimgupload/aivet";
    private String maxSize = "10MB";
    private String allowedTypes = "jpg,jpeg,png,webp";
    
    // Getter/Setter
    public String getBasePath() { return basePath; }
    public void setBasePath(String basePath) { this.basePath = basePath; }
    
    public String getMaxSize() { return maxSize; }
    public void setMaxSize(String maxSize) { this.maxSize = maxSize; }
    
    public String getAllowedTypes() { return allowedTypes; }
    public void setAllowedTypes(String allowedTypes) { this.allowedTypes = allowedTypes; }
    
    // 유틸리티 메소드
    public String[] getAllowedTypesArray() {
        return allowedTypes.split(",");
    }
}