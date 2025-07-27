package petcare.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupabaseFileDTO {
    private String name;
    private String id;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime lastAccessedAt;
    private Object metadata;
}

@Data
@NoArgsConstructor
@AllArgsConstructor 
class SupabaseFileListResponse {
    private List<SupabaseFileDTO> files;
    private String message;
    private boolean error;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class SupabaseUploadResponse {
    private String Key;
    private String Id;
    private String publicUrl;
    private String fullPath;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class SupabaseUploadResult {
    private List<SupabaseUploadResponse> uploads;
    private List<String> errors;
    private String message;
    private boolean success;
} 