package petcare.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import petcare.dto.ShelterDTO;

import java.util.List;

@Mapper
public interface ShelterMapper {

    @Select("SELECT DISTINCT breed, shelter, shelter_call_number, shelter_address, WGS84_latitude, WGS84_longitude " +
            "FROM pet_find_info ")

    List<ShelterDTO> ShelterList();
    
} 
