package petcare.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import petcare.config.ImageUploadConfig;
import org.springframework.beans.factory.annotation.Value;
import petcare.service.SupabaseStorageService;
import petcare.mapper.AiVetMapper;
import petcare.dto.ImageDTO;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ImageUploadService {
    

    @Autowired
    private SupabaseStorageService supabaseStorageService;

    @Value("${supabase.storage.folder-chatbot-images}")
    private String chatbotImagesFolder;
    
    @Autowired
    private AiVetMapper aiVetMapper;
    
    public List<String> saveImages(List<MultipartFile> images, int messageId, String sessionId) {
        List<String> savedPaths = new ArrayList<>();
        
        if (images == null || images.isEmpty()) {
            return savedPaths;
        }
        
        try {
            for (MultipartFile image : images) {
                String savedPath = saveImage(image, messageId, sessionId);
                if (savedPath != null) {
                    savedPaths.add(savedPath);
                }
            }
        } catch (Exception e) {
            System.err.println("이미지 저장 오류: " + e.getMessage());
        }
        
        return savedPaths;
    }
    
    private String saveImage(MultipartFile image, int messageId, String sessionId) throws IOException {
        // 파일명 생성: yyyyMMddHH_sessionPrefix_msg메시지아이디_randomId.확장자
        String timePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
        String originalName = image.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf('.'));
        }
        String sessionPrefix = sessionId.substring(0, 8);
        String randomId = UUID.randomUUID().toString().substring(0, 8);
        String fileName = timePrefix + "_" + sessionPrefix + "_msg" + messageId + "_" + randomId + extension;
        System.out.println("[ImageUploadService] 업로드 시도: fileName=" + fileName + ", originalName=" + originalName + ", sessionId=" + sessionId);
        // Supabase Storage에 업로드
        List<MultipartFile> uploadList = Collections.singletonList(image);
        List<String> fileNames = Collections.singletonList(fileName);
        Map<String, Object> uploadResult = supabaseStorageService.uploadFiles(uploadList, chatbotImagesFolder, fileNames);
        System.out.println("[ImageUploadService] Supabase 업로드 결과: " + uploadResult);
        // 업로드 결과에서 URL 또는 경로 추출 (예시: 첫 번째 파일의 URL)
        String uploadedUrl = null;
        if (uploadResult != null && uploadResult.containsKey("uploads")) {
            Object uploadsObj = uploadResult.get("uploads");
            if (uploadsObj instanceof List && !((List<?>) uploadsObj).isEmpty()) {
                Object firstUpload = ((List<?>) uploadsObj).get(0);
                if (firstUpload instanceof Map && ((Map<?, ?>) firstUpload).containsKey("publicUrl")) {
                    uploadedUrl = ((Map<?, ?>) firstUpload).get("publicUrl").toString();
                }
            }
        }
        // DB에 저장
        ImageDTO imageDTO = new ImageDTO();
        imageDTO.setMessageId(messageId);
        imageDTO.setImageFilename(fileName);
        imageDTO.setImagePath(uploadedUrl != null ? uploadedUrl : fileName);
        imageDTO.setImageComment("AI 진단용 이미지");
        aiVetMapper.insertImage(imageDTO);
        return uploadedUrl != null ? uploadedUrl : fileName;
    }
    
}