package petcare.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import petcare.dto.PetImageDTO;
import petcare.dto.PetProfileDTO;

@Mapper
public interface PetManagerMapper {
    int addPetProfile(PetProfileDTO petProfile);
    int updatePetProfile(PetProfileDTO petProfile);
    int addPetImages(List<PetImageDTO> petImages);
    ArrayList<PetProfileDTO> getPetProfileList();
    PetProfileDTO getPetProfile(Long petUid);
    ArrayList<PetImageDTO> getPetImages(Long petUid);
    int deletePetProfile(Long petUid);
    int deletePetImages(Map<String, Object> params);
    int savePetProfileHTML(Map<String, Object> params);
    Map<String, Object> getPetProfileHTML(Long petUid);
    int updatePetImages(PetImageDTO petImage);
    int deletePetProfileHTML(Long petUid);
}
