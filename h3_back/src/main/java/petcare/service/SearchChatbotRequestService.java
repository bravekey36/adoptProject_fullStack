package petcare.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import petcare.dto.SearchChatbotRequestDTO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@Service
public class SearchChatbotRequestService {
    
    private final RestTemplate restTemplate;

    @Value("${fastapi.search.server}")
    private String fastApiUrl; // http://localhost:8000

    public SearchChatbotRequestService(RestTemplateBuilder builder) {

        this.restTemplate = builder.build();
    }

    public ResponseEntity<?> fowardToFastApi(SearchChatbotRequestDTO request) {

        try {
            String url = fastApiUrl + "/gemini-chat";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<SearchChatbotRequestDTO> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("FastAPI 서버 호출 중 오류 발생: " + e.getMessage());
        }
    }

    public ResponseEntity<?> closeSessionInFastApi() {

        try {
            String url = fastApiUrl + "/close-session";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>("{}", headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("세션 종료중 오류 발생" + e.getMessage());
        }
    }
}
