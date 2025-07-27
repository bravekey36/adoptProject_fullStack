package petcare.service;

import petcare.dto.PetListRandomDTO;
import petcare.mapper.PetListRandomMapper;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PetListRandomService {
    
    private final PetListRandomMapper petListRandomMapper;

    public List<PetListRandomDTO> getRandomPets() {
        return petListRandomMapper.selectRandomPetList();
    }
}
