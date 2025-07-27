package petcare.service;

import petcare.dto.PetcareDTO;
import petcare.mapper.PetcareMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.List;

@Service
public class PetcareService {
	
    @Value("${fastapi.search.server}")
    private String fastapi_url;

    @Autowired
    private PetcareMapper mapper;
    
    public List<PetcareDTO> getDataTest() {
    	
    	return mapper.getTop10Status();
    }
    
    public String sendToFastAPI(List<PetcareDTO> dataList) {
        RestTemplate restTemplate = new RestTemplate();
        String fastApiUrl =  fastapi_url + "/process";
    	
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<List<PetcareDTO>> request = new HttpEntity<>(dataList, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(fastApiUrl, request, String.class);
        
    	return response.getBody();
    }
}
