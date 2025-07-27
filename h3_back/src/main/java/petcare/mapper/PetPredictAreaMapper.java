package petcare.mapper;

import petcare.dto.PetPredictAreaDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PetPredictAreaMapper {
    PetPredictAreaDTO getAreaByShelterName(@Param("shelterName") String sheltername);
}
