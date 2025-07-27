package petcare.controller;

import petcare.dto.PetSearchAreaDTO;
import petcare.service.PetSearchAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/petcare")
public class PetSearchAreaController {
    
    @Autowired
    private PetSearchAreaService petSearchAreaService;

	@PostMapping("/searchArea")
	public List<PetSearchAreaDTO> getAreaByShelterName(@RequestBody Map<String, String> payload) {
	    String shelterName = payload.get("name");
	    return petSearchAreaService.getAreaByShelterName(shelterName);
	}
}
