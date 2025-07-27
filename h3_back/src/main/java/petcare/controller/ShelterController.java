package petcare.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import petcare.dto.ShelterDTO;
import petcare.service.ShelterService;

@RestController
@RequestMapping("/petcare")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // React에서 호출 허용
public class ShelterController {

    private final ShelterService shelterService;

    @PostMapping("/shelterList")
    public List<ShelterDTO> getShelterList() {
        return shelterService.getShelterLists();
    }
    
}
