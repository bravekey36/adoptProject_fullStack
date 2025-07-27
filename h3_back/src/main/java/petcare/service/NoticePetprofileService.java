package petcare.service;

import petcare.dto.NoticePetprofileDTO;
import petcare.mapper.NoticePetprofileMapper;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticePetprofileService {

    private final NoticePetprofileMapper noticePetprofileMapper;

    public List<NoticePetprofileDTO> getPetprofiles() {
        return noticePetprofileMapper.selectNoticePetprofile();
    }
    
}
