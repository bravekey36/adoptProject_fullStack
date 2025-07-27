package petcare.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import petcare.dto.PetMissingInfoDTO;
import petcare.mapper.PetMissingInfoMapper;

@Service
public class PetMissingInfoService {
    
    @Autowired
	private PetMissingInfoMapper petMissingInfoMapper;
	
	
    public int saveMissingPetInfo(PetMissingInfoDTO petMissingInfoDTO) {
        return petMissingInfoMapper.insertMissingPetInfo(petMissingInfoDTO);
    }
}
