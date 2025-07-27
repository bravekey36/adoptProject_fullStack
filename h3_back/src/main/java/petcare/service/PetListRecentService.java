package petcare.service;

import petcare.dto.PetListRecentDTO;
import petcare.mapper.PetListRecentMapper;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PetListRecentService {
    
    private final PetListRecentMapper petListRecentMapper;

    public List<PetListRecentDTO> getRecentPets() {
        return petListRecentMapper.selectRecentPetsWithImagesAndShelter();
    }
}
