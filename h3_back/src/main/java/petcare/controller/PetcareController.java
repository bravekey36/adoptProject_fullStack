package petcare.controller;


import petcare.dto.PetcareDTO;
import petcare.mapper.PetcareMapper;
import petcare.service.PetcareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

// 프론트 서버 실행 주소
@RequestMapping("/petcare")
@RestController
public class PetcareController {
	
    private final PetcareMapper petcareTestMapper;
    private final RestTemplate restTemplate;
    
    public PetcareController(PetcareMapper petcareTestMapper) {
        this.petcareTestMapper = petcareTestMapper;
        this.restTemplate = new RestTemplate();
    }
    
    @Autowired
    private PetcareService service;
    
    @GetMapping("/")
    public String home() {
        return "기본포트 정상 작동중";
    }
    
    @GetMapping("/test")
    public List<PetcareDTO> getTop10Status() {
        return petcareTestMapper.getTop10Status();
    }
    
    @GetMapping("/fastapi")
    public String getDataFromFastAPI() {
        List<PetcareDTO> dataList = service.getDataTest();
        return service.sendToFastAPI(dataList);
    }
    
    @PostMapping("/AIprocess")
    public Map<String, Object> processWithAI(@RequestBody Map<String, Object> request) {
        List<PetcareDTO> dbData = petcareTestMapper.getTop10Status();
        Map<String, Object> fastApiRequest = new HashMap<>();
        fastApiRequest.put("query", request.get("query"));
        fastApiRequest.put("db_data", dbData);
        String fastApiUrl = "http://localhost:8000/api/llm-process";
        Map<String, Object> fastApiResponse = restTemplate.postForObject(fastApiUrl, fastApiRequest, Map.class);
        Map<String, Object> response = new HashMap<>();
        response.put("db_data", dbData);
        response.put("llm_result", fastApiResponse);
        return response;
    }



    
}
