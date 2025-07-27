package petcare.service;

import petcare.dto.NoticePetfindinfoDTO;
import petcare.mapper.NoticePetfindinfoMapper;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticePetfindinfoService {

    private final NoticePetfindinfoMapper noticePetfindinfoMapper;

    public List<NoticePetfindinfoDTO> getPetfindinfos() {
        return noticePetfindinfoMapper.selectNoticePetfindinfo();
    }
    
}
