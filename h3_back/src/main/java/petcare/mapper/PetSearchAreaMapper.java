package petcare.mapper;

import petcare.dto.PetSearchAreaDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PetSearchAreaMapper {
    
    // PetSearchAreaxml로부터 받아온 데이터틀 가지고 사용자가 지정한 보호소명으로 검색
    List<PetSearchAreaDTO> findAreaByShelterName(@Param("shelterName") String shelterName);
}
