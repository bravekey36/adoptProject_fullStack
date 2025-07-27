package petcare.service;


import petcare.dto.PetListRandomDTO;
import petcare.dto.PetListUserSelectDTO;
import petcare.mapper.PetListUserSelectMapper;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PetListUserSelectService {
    
    private final PetListUserSelectMapper petListUserSelectMapper;

    public List<PetListRandomDTO> searchPets(PetListUserSelectDTO params) {
        return petListUserSelectMapper.searchPets(params);
    }
}
