package petcare.service;

import petcare.dto.PetListShelterListDTO;
import petcare.mapper.PetListShelterMapper;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PetListShelterService {
    
    private final PetListShelterMapper petListShelterMapper;

    public List<PetListShelterListDTO> getShelterList() {
        return petListShelterMapper.selectShelterforPetList();
    }
}
