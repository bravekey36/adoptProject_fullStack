package petcare.service;

import petcare.dto.PetSearchAreaDTO;
import petcare.mapper.PetSearchAreaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PetSearchAreaService {
    
    @Autowired
    private PetSearchAreaMapper petSearchAreaMapper;

    public List<PetSearchAreaDTO> getAreaByShelterName(String shelterName) {
        return petSearchAreaMapper.findAreaByShelterName(shelterName);
    }
}
