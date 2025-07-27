package petcare.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import petcare.config.SupabaseConfig;
import petcare.dto.SupabaseFileDTO;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SupabaseStorageService {
    
    @Autowired
    private SupabaseConfig supabaseConfig;
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    public SupabaseStorageService() {
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 지정된 폴더의 파일 리스트 조회
     * @param folderPath 폴더 경로 (예: "images/pets/")
     * @return 파일 리스트
     */
    public Map<String, Object> getFileList(String folderPath) {
        try {
            String url = supabaseConfig.getStorageApiUrl() + "/object/list/" + supabaseConfig.getBucket();
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("prefix", folderPath);
            requestBody.put("limit", 100);
            requestBody.put("offset", 0);
            
            String response = webClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseConfig.getSecretKey())
                    .header("apikey", supabaseConfig.getSecretKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            List<SupabaseFileDTO> files = objectMapper.readValue(response, new TypeReference<List<SupabaseFileDTO>>() {});
            
            // 파일에 public URL 추가
            List<Map<String, Object>> filesWithUrls = files.stream()
                    .filter(file -> file.getName() != null && !file.getName().endsWith("/")) // 폴더 제외
                    .map(file -> {
                        Map<String, Object> fileInfo = new HashMap<>();
                        fileInfo.put("name", file.getName());
                        fileInfo.put("id", file.getId());
                        fileInfo.put("updatedAt", file.getUpdatedAt());
                        fileInfo.put("createdAt", file.getCreatedAt());
                        fileInfo.put("publicUrl", supabaseConfig.getPublicUrl(file.getName()));
                        return fileInfo;
                    })
                    .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("files", filesWithUrls);
            result.put("count", filesWithUrls.size());
            result.put("folderPath", folderPath);
            
            return result;
            
        } catch (WebClientResponseException e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "API Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            errorResult.put("files", new ArrayList<>());
            return errorResult;
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "Unexpected error: " + e.getMessage());
            errorResult.put("files", new ArrayList<>());
            return errorResult;
        }
    }

    public Map<String, Object> uploadFiles(List<MultipartFile> files) {
        // [팀 공용] 기존 방식: 자동 파일명 규칙으로 업로드됩니다.
        // 파일명 커스텀 필요시 아래 오버로드 함수(3개 인자) 사용
        return uploadFiles(files, supabaseConfig.getFolderPetImages(), null);
    }

    /**
     * 여러 파일 업로드
     * @param files 업로드할 파일들
     * @param folderPath 업로드할 폴더 경로 (예: "images/pets/")
     * @return 업로드 결과 (파일명, public URL 포함)
     */
    public Map<String, Object> uploadFiles(List<MultipartFile> files, String folderPath, List<String> fileNames) {
        // [팀 공용/확장] fileNames가 null이면 기존 자동 파일명 규칙, 값이 있으면 해당 파일명으로 업로드됩니다.
        // 팀원은 기존처럼 사용, 챗봇 등 특수 목적은 커스텀 파일명 지정 가능
        List<Map<String, Object>> uploadResults = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        if (supabaseConfig.getSecretKey() == null) {
            throw new RuntimeException("Supabase  환경 변수를 확인하세요.(secret-key)");
        }

        if (files == null || files.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "업로드할 파일이 없습니다.");
            result.put("uploads", uploadResults);
            result.put("errors", errors);
            return result;
        }

        // 폴더 경로 정리 (끝에 /가 없으면 추가)
        if (!folderPath.isEmpty() && !folderPath.endsWith("/")) {
            folderPath += "/";
        }

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String customFileName = (fileNames != null && fileNames.size() > i) ? fileNames.get(i) : null;
            try {
                if (file.isEmpty()) {
                    errors.add("빈 파일이 포함되어 있습니다: " + file.getOriginalFilename());
                    continue;
                }

                String originalFilename = file.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
                }

                String filename;
                if (customFileName != null) {
                    filename = customFileName;
                } else {
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                    String uuid = UUID.randomUUID().toString().substring(0, 8);
                    filename = timestamp + "_" + uuid + extension;
                }
                String fullPath = folderPath + filename;

                // Supabase Storage에 업로드
                String uploadUrl = supabaseConfig.getStorageApiUrl() + "/object/" + supabaseConfig.getBucket() + "/" + fullPath;

                byte[] fileBytes = file.getBytes();
                String contentType = file.getContentType();
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                String response = webClient.post()
                        .uri(uploadUrl)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseConfig.getSecretKey())
                        .header("apikey", supabaseConfig.getSecretKey())
                        .contentType(MediaType.parseMediaType(contentType))
                        .bodyValue(fileBytes)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                // 업로드 성공 시 결과 추가
                Map<String, Object> uploadResult = new HashMap<>();
                uploadResult.put("fileName", originalFilename);
                uploadResult.put("publicUrl", supabaseConfig.getPublicUrl(fullPath));
                uploadResult.put("fileSize", file.getSize());
                uploadResults.add(uploadResult);

            } catch (WebClientResponseException e) {
                String errorMsg = "파일 업로드 실패: " + file.getOriginalFilename() + " - " + e.getStatusCode() + ": " + e.getResponseBodyAsString();
                errors.add(errorMsg);
                System.err.println("WebClient 에러: " + errorMsg);
                System.err.println("요청 헤더: " + e.getRequest());
            } catch (IOException e) {
                String errorMsg = "파일 읽기 실패: " + file.getOriginalFilename() + " - " + e.getMessage();
                errors.add(errorMsg);
                System.err.println("IO 에러: " + errorMsg);
            } catch (Exception e) {
                String errorMsg = "예상치 못한 오류: " + file.getOriginalFilename() + " - " + e.getMessage();
                errors.add(errorMsg);
                System.err.println("일반 에러: " + errorMsg);
                e.printStackTrace();
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalFiles", files.size());
        result.put("successCount", uploadResults.size());
        result.put("uploads", uploadResults);
        result.put("errors", errors);
        return result;
    }
    
    /**
     * 파일 삭제
     * @param filePath 삭제할 파일 경로
     * @return 삭제 결과
     */
    public Map<String, Object> deleteFile(String folder, String filePath) {
        try {
            String url = supabaseConfig.getStorageApiUrl() + "/object/" + supabaseConfig.getBucket() + "/" + folder + "/" + filePath;
            
            String response = webClient.delete()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseConfig.getSecretKey())
                    .header("apikey", supabaseConfig.getSecretKey())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            System.out.println("response : " + response);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "파일이 성공적으로 삭제되었습니다.");
            result.put("filePath", filePath);
            
            return result;
            
        } catch (WebClientResponseException e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "파일 삭제 실패: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            errorResult.put("filePath", filePath);
            return errorResult;
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "예상치 못한 오류: " + e.getMessage());
            errorResult.put("filePath", filePath);
            return errorResult;
        }
    }
} 