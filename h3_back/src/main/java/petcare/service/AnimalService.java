package petcare.service;

import lombok.RequiredArgsConstructor;
import petcare.dto.AnimalDTO;
import petcare.dto.PetCoordinateInfoDTO;
import petcare.mapper.AnimalMapper;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnimalService {
    
    private final AnimalMapper animalMapper;

    public List<AnimalDTO> getTargetAnimals() {
        return animalMapper.findTargetAnimals();
    }

    public void saveCoordinates(PetCoordinateInfoDTO dto) {
        animalMapper.updateCoordinate(dto);
    }
}
