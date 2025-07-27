package petcare.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import petcare.service.PetPredictAreaService;

import java.util.Map;

@RestController
@RequestMapping("/petcare")
@RequiredArgsConstructor
public class PetPredictAreaController {

    private final PetPredictAreaService petPredictAreaService;

    @PostMapping("/predictArea")
    public ResponseEntity<Map<String, Object>> predictArea(@RequestBody Map<String, String> request) {
        String shelterName = request.get("name");

        Map<String, Object> result = petPredictAreaService.predictAreaByShelter(shelterName);

        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }
}
