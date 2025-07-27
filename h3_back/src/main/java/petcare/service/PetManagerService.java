package petcare.service;

import petcare.mapper.PetManagerMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import petcare.dto.PetImageDTO;
import petcare.dto.PetProfileDTO;
import petcare.service.SupabaseStorageService;
import java.util.Map;

@Service
public class PetManagerService {
	
    @Autowired
    private PetManagerMapper mapper;
    
    @Value("${supabase.storage.folder-pet-images}")
    private String petImagesFolder;
	
    @Value("${supabase.storage.url}")
    private String supabaseStorageUrl;

    @Value("${fastapi.imgsearch.server}")
    private String vectorizeServerUrl;

    @Value("${fastapi.best-image.server}")
    private String bestImageServerUrl;

    @Value("${service.on.best-image-select:true}")
    private boolean serviceOnBestImageSelect;

    @Autowired
    private SupabaseStorageService supabaseStorageService;
    
    public Long addPetProfile(PetProfileDTO petProfile) {
        mapper.addPetProfile(petProfile);
        return petProfile.getPetUid();
    }

    public Long updatePetProfile(PetProfileDTO petProfile) {
        mapper.updatePetProfile(petProfile);
        return petProfile.getPetUid();
    }

    public Integer addPetImages(List<PetImageDTO> petImages) {
        if (petImages == null || petImages.isEmpty()) return 0;

        Integer affectedRows = mapper.addPetImages(petImages);
        return affectedRows;
    }

    public Integer updatePetImages(List<PetImageDTO> petImages) {
        if (petImages == null || petImages.isEmpty()) return 0;
        Integer affectedRows = 0;
        for (PetImageDTO petImage : petImages) {
            affectedRows += mapper.updatePetImages(petImage);
        }
        return affectedRows;
    }

    public List<PetProfileDTO> getPetProfileList() {
        List<PetProfileDTO> resultList = mapper.getPetProfileList();
        return resultList;
    }

    public PetProfileDTO getPetProfile(Long petUid) {
        PetProfileDTO result = mapper.getPetProfile(petUid);
        return result;
    }

    public ArrayList<PetImageDTO> getPetImages(Long petUid) {
        ArrayList<PetImageDTO> resultList = mapper.getPetImages(petUid);
        return resultList;
    }

    public int deletePetProfile(Long petUid) {
        return mapper.deletePetProfile(petUid);
    }

    public int deletePetImages(Long petUid, ArrayList<Long> deleteImageIds) {
        ArrayList<PetImageDTO> imageList = this.getPetImages(petUid);
        if (imageList == null || imageList.isEmpty()) {
            return 0;
        }

        for (PetImageDTO image : imageList) {
            if (deleteImageIds == null || (deleteImageIds.contains(image.getPetImageId()))) {
                String imageUrl = image.getImageUrl();
                if (imageUrl != null && imageUrl.startsWith(supabaseStorageUrl)) {
                    String storageFileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                    Map<String, Object> resultMap = supabaseStorageService.deleteFile(petImagesFolder, storageFileName);
                    System.out.println(resultMap);
                }
            }
        }

        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            return mapper.deletePetImages(Map.of("petUid", petUid, "deleteImageIds", deleteImageIds));
        } else {
            return mapper.deletePetImages(Map.of("petUid", petUid));
        }
    }

    public int savePetProfileHTML(Long petUid, String html) {
        return mapper.savePetProfileHTML(Map.of("petUid", petUid, "html", html));
    }

    public Map<String, Object> getPetProfileHTML(Long petUid) {
        return mapper.getPetProfileHTML(petUid);
    }


    /**
     * 이미지 벡터 데이터 생성
     * @throws RuntimeException connection 오류 시
     */
    public String getPetImageVector(String imageUrl) {
        String api = "/api/extract_features_from_url";
        String requestUrl = vectorizeServerUrl + api;

        // 이미지 벡터 데이터 생성
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(3000);

            // Create JSON request body
            String jsonInputString = "{\"imageUrl\":\"" + imageUrl + "\"}";
            
            try (var os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Get response
            StringBuilder response = new StringBuilder();
            try (var br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            // Extract "vector" value from JSON response
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.toString());
            @SuppressWarnings("unchecked")
            ArrayList<Double> vector = objectMapper.convertValue(jsonNode.get("vector"), ArrayList.class);

            System.out.println(">>>>> vector: " + vector);
            return vector.toString();

        } catch (java.io.IOException e) {
            System.err.println("❌ [getPetImageVector] Connection error: " + e.getMessage());
            throw new RuntimeException("이미지 벡터 생성 서버 연결 실패!");
        } catch (RuntimeException e) {
            System.err.println("❌ [getPetImageVector] Connection error: " + e.getMessage());
            throw new RuntimeException("이미지 벡터 생성 서버 연결 실패!");
        } catch (Exception e) {
            System.err.println("❌ [getPetImageVector] Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 보호견 대표이미지 선택
     * @throws RuntimeException connection 오류 시
     */
    public int getBestImageIndex(List<PetImageDTO> petImages) {
        int bestImageIndex = -1;
        String api = "/api/select-best-image";
        String requestUrl = this.bestImageServerUrl + api;
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(3000);

            // Create JSON request body using ObjectMapper
            List<String> imageUrls = new ArrayList<>();
            for (PetImageDTO petImage : petImages) {
                imageUrls.add(petImage.getImageUrl());
            }
            
            Map<String, Object> requestBody = Map.of("imageUrls", imageUrls);
            String jsonInputString = objectMapper.writeValueAsString(requestBody);
            
            System.out.println(">>>>> [getBestImageIndex] Request JSON: " + jsonInputString);
            
            try (var os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Get response
            StringBuilder response = new StringBuilder();
            try (var br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            // Extract mainImageUrl from JSON response
            JsonNode jsonNode = objectMapper.readTree(response.toString());
            //@SuppressWarnings("unchecked")
            String mainImageUrl = objectMapper.convertValue(jsonNode.get("url"), String.class);

            System.out.println(">>>>> mainImageUrl: " + mainImageUrl);

            for (PetImageDTO petImage : petImages) {
                if (petImage.getImageUrl().equals(mainImageUrl)) {
                    bestImageIndex = petImages.indexOf(petImage);
                    break;
                }
            }
            System.out.println(">>>>> [PetManagerService] bestImageIndex: " + bestImageIndex + " / " + mainImageUrl);
            return bestImageIndex < 0 ? 0 : bestImageIndex;

        } catch (java.io.IOException e) {
            System.err.println("❌ [getBestImageIndex] Connection error: " + e.getMessage());
            throw new RuntimeException("대표이미지 선택 서버 연결 실패!");
        } catch (Exception e) {
            System.err.println("❌ [getBestImageIndex] Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 보호견 대표이미지 설정
     */
    public int updateBestPetImage(Long petUid) {
        ArrayList<PetImageDTO> imageList = this.getPetImages(petUid);
        if (imageList == null || imageList.isEmpty()) return -1;

        // 대표이미지 선택
        int bestImageIndex = serviceOnBestImageSelect && imageList.size() > 1 ? this.getBestImageIndex(imageList) : 0;
        System.out.println("[대표이미지 선택] petUid: " + petUid + " / bestImageIndex: " + bestImageIndex);

        // 기존 isBestImage 초기화
        for (PetImageDTO image : imageList) {
            image.setIsBestImage("N");
        }
        // isBestImage 설정
        imageList.get(bestImageIndex).setIsBestImage("Y");
        this.updatePetImages(imageList);

        return bestImageIndex;
    }

    public int deletePetProfileHTML(Long petUid) {
        return mapper.deletePetProfileHTML(petUid);
    }
}
