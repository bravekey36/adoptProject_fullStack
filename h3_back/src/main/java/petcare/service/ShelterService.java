package petcare.service;

import lombok.RequiredArgsConstructor;
import petcare.dto.ShelterDTO;
import petcare.mapper.ShelterMapper;


import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShelterService {
    
    private final ShelterMapper shelterMapper;

    public List<ShelterDTO> getShelterLists() {
        return shelterMapper.ShelterList();
    }
}
