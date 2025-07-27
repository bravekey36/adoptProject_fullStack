package petcare.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import petcare.dto.AnimalDTO;
import petcare.dto.PetCoordinateInfoDTO;
import petcare.service.AnimalService;


@RestController
@RequestMapping("/api/animals")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // React에서 호출 허용
public class AnimalController {
    
    private final AnimalService animalService;

    @GetMapping("/target-animals")
    public List<AnimalDTO> getTargetAnimals() {
        return animalService.getTargetAnimals();
    }

    @PostMapping("/save-coordinates")
    public String saveCoordinates(@RequestBody PetCoordinateInfoDTO dto) {
        animalService.saveCoordinates(dto);
        return "Coordinates saved successfully";
    }
}
