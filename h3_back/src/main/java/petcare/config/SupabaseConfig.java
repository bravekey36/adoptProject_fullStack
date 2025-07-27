package petcare.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "supabase.storage")
public class SupabaseConfig {
    
    private String url;
    private String anonKey;
    private String secretKey;
    private String bucket;
    private String folderPetImages;
    
    // Getters and Setters
    
    
    // 헬퍼 메소드들
    public String getStorageApiUrl() {
        return url + "/storage/v1";
    }
    
    public String getObjectUrl(String path) {
        return url + "/storage/v1/object/" + bucket + "/" + path;
    }
    
    public String getPublicUrl(String path) {
        return url + "/storage/v1/object/public/" + bucket + "/" + path;
    }
} 