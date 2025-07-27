package petcare.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import petcare.service.SupabaseStorageService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/supabase/storage")
@CrossOrigin(origins = "*")
public class SupabaseStorageController {
    
    @Autowired
    private SupabaseStorageService supabaseStorageService;
    
    /**
     * 지정된 폴더의 파일 리스트 조회
     * GET /api/supabase/storage/files?folder=images/pets/
     * 
     * @param folder 조회할 폴더 경로 (기본값: "")
     * @return 파일 리스트와 메타데이터
     */
    @GetMapping("/files/{folder}")
    public ResponseEntity<Map<String, Object>> getFileList(
            @RequestParam(value = "folder", defaultValue = "") String folder) {
        
        try {
            Map<String, Object> result = supabaseStorageService.getFileList(folder);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            }
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "서버 오류: " + e.getMessage());
            errorResponse.put("files", new java.util.ArrayList<>());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 여러 파일 업로드
     * POST /api/supabase/storage/uploads
     * 
     * @param files 업로드할 파일들 (multipart/form-data)
     * @param folder 업로드할 폴더 경로 (기본값: "uploads/")
     * @return 업로드 결과 (파일명, public URL 포함)
     */
    @PostMapping("/uploads")
    public ResponseEntity<Map<String, Object>> uploadFiles(@RequestParam("files") List<MultipartFile> files) {
        
        try {
            Map<String, Object> result = supabaseStorageService.uploadFiles(files);
            
            if (((Integer) result.get("successCount")) == ((Integer) result.get("totalFiles"))) {
                return ResponseEntity.ok(result);
            } else {
                // 일부 성공, 일부 실패의 경우 206 Partial Content
                int successCount = (Integer) result.get("successCount");
                if (successCount > 0) {
                    return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(result);
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
                }
            }
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "서버 오류: " + e.getMessage());
            errorResponse.put("uploads", new java.util.ArrayList<>());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 파일 삭제
     * DELETE /api/supabase/storage/file
     * 
     * @param filePath 삭제할 파일 경로
     * @return 삭제 결과
     */
    @DeleteMapping("/file")
    public ResponseEntity<Map<String, Object>> deleteFile(@RequestParam("folder") String folder, @RequestParam("path") String filePath) {
        
        try {
            Map<String, Object> result = supabaseStorageService.deleteFile(folder, filePath);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            }
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "서버 오류: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * API 상태 확인
     * GET /api/supabase/storage/health
     * 
     * @return API 상태 정보
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "Supabase Storage API");
        response.put("timestamp", java.time.LocalDateTime.now());
        response.put("endpoints", Map.of(
            "getFiles", "GET /api/supabase/storage/files?folder={folderPath}",
            "uploadFiles", "POST /api/supabase/storage/uploads",
            "deleteFile", "DELETE /api/supabase/storage/file?path={filePath}"
        ));
        
        return ResponseEntity.ok(response);
    }
} 