package petcare.service;

import petcare.dto.PetListBreedListDTO;
import petcare.mapper.PetListBreedMapper;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PetListBreedService {
    
    private final PetListBreedMapper petListBreedMapper;

    public List<PetListBreedListDTO> getBreedList() {
        return petListBreedMapper.selectBreedforPetList();
    }

}
