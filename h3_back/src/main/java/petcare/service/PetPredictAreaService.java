package petcare.service;

import petcare.dto.PetPredictAreaDTO;
import petcare.dto.PetPredictAreaResultDTO;
import petcare.mapper.PetPredictAreaMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PetPredictAreaService {

    private final PetPredictAreaMapper petPredictAreaMapper;

    @Value("${fastapi.search.server}")
    private String fastApiServer;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> predictAreaByShelter(String shelterName) {
        // 1. DB에서 보호소 이름으로 데이터 조회
        PetPredictAreaDTO shelterData = petPredictAreaMapper.getAreaByShelterName(shelterName);

        if (shelterData == null) {
            return null;
        }

        // 2. FastAPI 예측 요청 URL
        String url = fastApiServer + "/predict";

        try {
            // 3. FastAPI로 보낼 JSON 문자열 생성 (로그용)
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(shelterData);
            System.out.println("🚀 FastAPI로 보낼 JSON: " + json);

            // 4. FastAPI 호출 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<PetPredictAreaDTO> request = new HttpEntity<>(shelterData, headers);

            ResponseEntity<PetPredictAreaResultDTO> response = restTemplate.postForEntity(
                url, request, PetPredictAreaResultDTO.class);

            PetPredictAreaResultDTO predictResult = response.getBody();

            // 5. DB 조회 데이터와 예측 결과를 합쳐서 Map으로 반환
            Map<String, Object> result = new HashMap<>();
            result.put("place", shelterData.getPlace());
            result.put("breed", shelterData.getBreed());
            result.put("age", shelterData.getAge());
            result.put("weight", shelterData.getWeight());
            result.put("shelter", shelterData.getShelter());
            result.put("shelter_address", shelterData.getShelter_address());
            result.put("WGS84_latitude", shelterData.getWGS84_latitude());
            result.put("WGS84_longitude", shelterData.getWGS84_longitude());
            result.put("place_latitude", shelterData.getPlace_latitude());
            result.put("place_longitude", shelterData.getPlace_longitude());
            result.put("pet_received_date", shelterData.getPet_received_date());

            if (predictResult != null) {
                result.put("predicted_cluster", predictResult.getPredicted_cluster());
                result.put("center_latitude", predictResult.getCenter_latitude());
                result.put("center_longitude", predictResult.getCenter_longitude());
                result.put("radius", predictResult.getRadius());
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("FastAPI 서버 호출 실패");
        }
    }
}
