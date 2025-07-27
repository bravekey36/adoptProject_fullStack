package petcare.controller;

// import petcare.service.CmnCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import petcare.service.GeminiService;
import petcare.service.PetManagerService;
import petcare.service.SupabaseStorageService;
import petcare.dto.GeminiResponseDTO;
import petcare.dto.PetImageDTO;
import petcare.dto.PetProfileDTO;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.regex.Matcher;

@RequestMapping("/manager")
@RestController
public class PetManagerController {
    @Autowired
    private PetManagerService petManagerService;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private SupabaseStorageService supabaseStorageService;

    @Autowired
    private Environment environment;

    @Value("${service.on.image-vectorize:true}")
    private boolean serviceOnImageVectorize;

    @Value("${service.on.best-image-select:true}")
    private boolean serviceOnBestImageSelect;
    
    // 클래스 레벨에 static 캐시 추가
    private static String sampleHtmlCache = null;

    // 보호견 목록 조회
    @RequestMapping(method = RequestMethod.GET, value = "/pet-list")
    public ResponseEntity<List<PetProfileDTO>> getPetProfileList() {
        List<PetProfileDTO> resultList = new ArrayList<>();
        resultList = petManagerService.getPetProfileList();

        return ResponseEntity.status(HttpStatus.OK).body(resultList);
    }

    // 보호견 등록
    @RequestMapping(method = RequestMethod.POST, value = "/pet", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Map<String, Object>> addPetProfile(@RequestPart(value="petProfile") @Validated PetProfileDTO petProfile,
                                                @RequestPart(value="photos", required=false) List<MultipartFile> photos) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success", false);

        Long petUid = null;
        List<PetImageDTO> petImages = new ArrayList<>();
        try {            
            // 1. 보호견 DB등록
            petUid = petManagerService.addPetProfile(petProfile);
            resultMap.put("petUid", petUid);

            // 2. 스토리지에 파일 업로드(publicUrl 생성)
            if (photos != null && !photos.isEmpty() && petUid != null) {
                Map<String, Object> result = supabaseStorageService.uploadFiles(photos);
                if (result.get("successCount") == null ||(Integer) result.get("successCount") == 0) {
                    throw new Exception("이미지 저장에 실패했습니다(파일 업로드 실패)");
                }

                // 3. 보호견 사진 DB저장
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> uploadList = (List<Map<String, Object>>) result.get("uploads");
                for (Map<String, Object> upload : uploadList) {
                    PetImageDTO petImage = new PetImageDTO(petUid, upload);
                    // fast-api : 이미지 벡터 생성(service=on 인 경우 적용)
                    if (serviceOnImageVectorize) {
                        String vector = petManagerService.getPetImageVector(petImage.getImageUrl());
                        if (vector == null) {
                            throw new Exception("이미지 저장에 실패했습니다(벡터 생성 실패)");
                        }
                        petImage.setImageVector(vector);
                    }
                    petImages.add(petImage);
                }

                // 사진 등록 필수
                if (petImages.size() < 1) {
                    throw new Exception("이미지 저장에 실패했습니다(사진 없음)");
                }
                petManagerService.addPetImages(petImages);
            }
            // 대표이미지 설정
            petManagerService.updateBestPetImage(petUid);
            resultMap.put("success", true);

        } catch (Exception e) {
            // DB저장 rollback
            if (petUid != null) {
                ArrayList<Long> deleteImageIds = new ArrayList<>();
                for (PetImageDTO petImage : petImages) {
                    deleteImageIds.add(petImage.getPetImageId());
                }
                petManagerService.deletePetImages(petUid, deleteImageIds.isEmpty() ? null : deleteImageIds);
                petManagerService.deletePetProfile(petUid);
            }

            resultMap.put("error", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultMap);
        }

        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }

    // 보호견 수정
    @RequestMapping(method = RequestMethod.PUT, value = "/pet", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Map<String, Object>> updatePetProfile(@RequestPart(value="petProfile") @Validated PetProfileDTO petProfile,
                                                @RequestPart(value="photos", required=false) List<MultipartFile> photos,
                                                @RequestPart(value="deleteImageIds", required=false) ArrayList<Long> deleteImageIds) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success", false);

        System.out.println(">>>> deleteImageIds: " + deleteImageIds);

        List<PetImageDTO> petImages = new ArrayList<>();
        Long petUid = petProfile.getPetUid();
        try {            
            // 1. 보호견 DB업데이트
            if (petUid == null) {
                throw new Exception("petUid is null");
            }
            petManagerService.updatePetProfile(petProfile);

            // 2. (정보가 변경될 수 있으므로)입양프로필 HTML 삭제
            petManagerService.deletePetProfileHTML(petUid);

            // 3. 사진 삭제
            if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
                petManagerService.deletePetImages(petUid, deleteImageIds);
            }

            // 4. 새 사진 스토리지 업로드(publicUrl) & 저장
            if (photos != null && !photos.isEmpty()) {
                Map<String, Object> result = supabaseStorageService.uploadFiles(photos);
                if (result.get("successCount") != null && (Integer) result.get("successCount") > 0) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> uploadList = (List<Map<String, Object>>) result.get("uploads");
                    for (Map<String, Object> upload : uploadList) {
                        PetImageDTO petImage = new PetImageDTO(petUid, upload);
                        // 5. fast-api : 이미지 벡터 생성(service=on 인 경우 적용)
                        if (serviceOnImageVectorize) {
                            String vector = petManagerService.getPetImageVector(petImage.getImageUrl());
                            petImage.setImageVector(vector);
                        }
                        petImages.add(petImage);
                    }
                    // DB저장
                    petManagerService.addPetImages(petImages);
                }
            }
            
            // 6. 대표이미지 설정
            petManagerService.updateBestPetImage(petUid);
            resultMap.put("success", true);

        } catch (Exception e) {
            resultMap.put("error", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultMap);
        }

        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }

    // 보호견 입양프로필 생성
    @RequestMapping(method = RequestMethod.GET, value = "/gen-profile/{petUid}")
    public ResponseEntity<Map<String, Object>> generatePetProfile(@PathVariable(value="petUid") String uid) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success", false);

        try {
            if (uid == null) {
                throw new Exception("petUid is null");
            }
            Long petUid = Long.parseLong(uid);
            PetProfileDTO petProfile = petManagerService.getPetProfile(petUid);
            Map<String, Object> petProfileMap = petProfile.toPetProfileMap();
            String petId = petProfile.getNoticeId() != null ? petProfile.getNoticeId() : "";
            List<PetImageDTO> images = petManagerService.getPetImages(petUid);
            List<PetImageDTO> photos = _validatePhotos(images);
            if (photos.isEmpty()) {
                resultMap.put("error", "유효한 사진 파일이 없습니다(최소 1장 필요)");
                return ResponseEntity.status(HttpStatus.OK).body(resultMap);
            }

            // 대표 이미지URL 추출
            String firstImageName = photos.get(0).getImageUrl();
            for (PetImageDTO photo : photos) {
                if (photo.getIsBestImage().equals("Y")) {
                    firstImageName = photo.getImageUrl();
                    System.out.println("대표 이미지 찾음: " + firstImageName);
                    break;
                }
            }

            // 1. Read template JSON file
            Resource resource = new ClassPathResource("gemini/prompts/adoption_profile_template.json");
            String templateJson = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()), "UTF-8");
            
            // 2. Parse JSON template
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> templateMap = mapper.readValue(templateJson, Map.class);
            
            // 3. Convert petProfile to JSON and read sample HTML
            String petInfoJson = mapper.writeValueAsString(petProfileMap);
            
            // 메서드에서 캐시 활용
            if (sampleHtmlCache == null) {
                sampleHtmlCache = new String(FileCopyUtils.copyToByteArray(
                    new ClassPathResource("gemini/prompts/adoption_profile_template_sample.min.html").getInputStream()), "UTF-8");
            }
            String sampleHtml = sampleHtmlCache;
            
            // 4. Replace placeholders in user message content
            List<Map<String, Object>> messages = (List<Map<String, Object>>) templateMap.get("messages");
            for (Map<String, Object> message : messages) {
                if ("user".equals(message.get("role"))) {
                    String content = (String) message.get("content");
                    // 체이닝 방식으로 두 플레이스홀더를 모두 치환
                    String updatedContent = content
                        .replace("{PET_INFO_JSON}", petInfoJson)
                        .replace("{SAMPLE_HTML}", sampleHtml)
                        .replace("{FIRST_IMAGE_NAME}", firstImageName)
                        .replace("{PET_ID}", petId);
                    message.put("content", updatedContent);
                    break;
                }
            }
            
            // 5. Extract system and user messages
            String systemMessage = null;
            String userMessage = null;
            
            for (Map<String, Object> message : messages) {
                if ("system".equals(message.get("role"))) {
                    systemMessage = (String) message.get("content");
                } else if ("user".equals(message.get("role"))) {
                    userMessage = (String) message.get("content");
                }
            }
            
            // 6. Extract config settings
            Map<String, Object> configMap = (Map<String, Object>) templateMap.get("config");
            Double temperature = configMap != null ? ((Number) configMap.get("temperature")).doubleValue() : null;
            Integer maxTokens = configMap != null ? (Integer) configMap.get("max_tokens") : null;
            
            //System.out.println("userMessage: " + userMessage);
            //System.out.println("Using temperature: " + temperature + ", maxTokens: " + maxTokens);
            
            // 7. Call Gemini API with structured messages, config, and photos
            List<String> imageUrls = new ArrayList<>();
            for (PetImageDTO photo : photos) {
                imageUrls.add(photo.getImageUrl());
            }
            GeminiResponseDTO response = geminiService.structuredQueryWithConfigUrls(
                systemMessage, userMessage, imageUrls, temperature, maxTokens);
            
            if (!response.isSuccess()) {
                resultMap.put("error", "Failed to generate profile: " + response.getError());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultMap);
            }
            
            // HTML만 추출 후 이미지 경로 수정
            String responseContent = response.getContent();
            String htmlContent = extractHtmlFromResponse(responseContent);

            // HTML 압축: 불필요한 공백과 주석 제거
            htmlContent = htmlContent.replaceAll("\\s+", " ")                  // 연속된 공백을 하나로
                                   .replaceAll("<!--.*?-->", "")               // HTML 주석 제거
                                   .replaceAll(">\\s+<", "><")                 // 태그 사이 공백 제거
                                   .replaceAll("^\\s+|\\s+$", "")             // 시작과 끝의 공백 제거
                                   .trim();
            petManagerService.savePetProfileHTML(petUid, htmlContent);

            resultMap.put("success", true);
            resultMap.put("htmlContent", htmlContent);
            return ResponseEntity.ok(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("error", "Error generating pet profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultMap);
        }
    }

    // 보호견 입양프로필 생성 후 HTML 추출
    private String extractHtmlFromResponse(String responseContent) {
        // 1. 마크다운 코드블록 제거
        responseContent = responseContent.replaceAll("```html\\s*", "").replaceAll("```\\s*$", "");
        
        // 2. HTML DOCTYPE 또는 html 태그 찾기
        Pattern htmlPattern = Pattern.compile("(<!DOCTYPE html.*?</html>|<html.*?</html>)", 
                                            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = htmlPattern.matcher(responseContent);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        // 3. HTML이 없으면 기본 응답
        return "<html><body><p>유효한 HTML을 생성하지 못했습니다.</p><pre>" + 
               responseContent + "</pre></body></html>";
    }

    // 보호견 상세 조회
    @RequestMapping(method = RequestMethod.GET, value = "/pet/{petUid}")
    public ResponseEntity<Map<String, Object>> getPetProfile(@PathVariable(value="petUid") String petUid) {
        Map<String, Object> resultMap = new HashMap<>();
        PetProfileDTO result = petManagerService.getPetProfile(Long.parseLong(petUid));
        ArrayList<PetImageDTO> resultList = petManagerService.getPetImages(Long.parseLong(petUid));
        resultMap.put("petProfile", result);
        resultMap.put("petImages", resultList);
        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }

    // 보호견 정보 삭제
    @RequestMapping(method = RequestMethod.DELETE, value = "/pet/{petUid}")
    public ResponseEntity<Map<String, Object>> deletePetProfile(@PathVariable(value="petUid") String petUid) {
        Map<String, Object> resultMap = new HashMap<>();
        // 입양프로필 HTML 삭제
        int deleteProfileHTMLCount = petManagerService.deletePetProfileHTML(Long.parseLong(petUid));
        // 보호견 이미지 삭제
        int deleteImageCount = petManagerService.deletePetImages(Long.parseLong(petUid), null);
        // 보호견 정보 삭제
        int deleteProfileCount = petManagerService.deletePetProfile(Long.parseLong(petUid));
        resultMap.put("deleteProfileCount", deleteProfileCount);
        resultMap.put("deleteImageCount", deleteImageCount);
        resultMap.put("deleteProfileHTMLCount", deleteProfileHTMLCount);
        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }

    // 입양프로필 HTML 조회
    @RequestMapping(method = RequestMethod.GET, value = "/pet/{petUid}/profile")
    public ResponseEntity<Map<String, Object>> getPetProfileHTML(@PathVariable(value="petUid") String petUid) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> result = petManagerService.getPetProfileHTML(Long.parseLong(petUid));
        resultMap.put("found", result != null && !result.isEmpty());
        resultMap.put("profile", result.get("profileHtml") != null ? result.get("profileHtml") : "");
        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }

    private List<PetImageDTO> _validatePhotos(List<PetImageDTO> photos) {
        if (photos == null || photos.isEmpty()) {
            return new ArrayList<>();
        }
        List<PetImageDTO> validPhotos = new ArrayList<>();
    
        // 이미지 URL이 다운로드 가능한지 확인
        List<String> validImageUrls = new ArrayList<>();
        for (PetImageDTO photo : photos) {
            try {
                URL url = new URL(photo.getImageUrl());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD");
                int responseCode = connection.getResponseCode();
                // 302 Found, 200 OK
                if (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
                    validImageUrls.add(photo.getImageUrl());
                }
                connection.disconnect();
            } catch (Exception e) {
                // URL이 유효하지 않거나 연결 실패 시 무시
                continue;
            }
        }

        // 유효한 이미지가 없는 경우 예외 처리
        if (validImageUrls.isEmpty()) {
            return new ArrayList<>();
        }

        // photos 리스트를 유효한 이미지만 포함하도록 필터링
        validPhotos = photos.stream()
                .filter(photo -> validImageUrls.contains(photo.getImageUrl()))
                .collect(Collectors.toList());
        return validPhotos;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/gen-vector")
    public ResponseEntity<String> genImageVector(@RequestBody Map<String, Object> requestBody) {
        String imageUrl = (String) requestBody.get("imageUrl");
        String vector = petManagerService.getPetImageVector(imageUrl);
        System.out.println(">>>>>" + vector);
        return ResponseEntity.status(HttpStatus.OK).body(vector);
    }

    /**
     * [DEBUG] 데이터 마이그레이션용 엔드포인트
     */
    @RequestMapping(method = RequestMethod.POST, value = "/update-best-image")
    public ResponseEntity<List<Map<String, Object>>> genBestImage(@RequestBody Map<String, Object> petUidRange) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        Integer petUidFrom = (Integer) petUidRange.get("petUidFrom");
        Integer petUidTo = (Integer) petUidRange.get("petUidTo");
        if (petUidFrom == null || petUidTo == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        
        for (Integer petUid = petUidFrom; petUid <= petUidTo; petUid++) {
            int bestImageIndex = petManagerService.updateBestPetImage(petUid.longValue());
            resultList.add(Map.of("petUid", petUid, "bestImageIndex", bestImageIndex));
        }
        return ResponseEntity.status(HttpStatus.OK).body(resultList);
    }
}