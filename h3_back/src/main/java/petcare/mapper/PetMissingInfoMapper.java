package petcare.mapper;

import org.apache.ibatis.annotations.Mapper;
import petcare.dto.PetMissingInfoDTO;

@Mapper
public interface PetMissingInfoMapper {

    int insertMissingPetInfo(PetMissingInfoDTO petMissingInfoDTO);
}
