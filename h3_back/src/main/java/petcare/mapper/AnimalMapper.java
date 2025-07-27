package petcare.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import petcare.dto.AnimalDTO;
import petcare.dto.PetCoordinateInfoDTO;

import java.util.List;

@Mapper
public interface AnimalMapper {

    @Select("SELECT id, place " +
            "FROM pet_coordinate_info ")
    List<AnimalDTO> findTargetAnimals();

    @Update("UPDATE pet_coordinate_info " +
        "SET place_latitude = #{place_latitude}, place_longitude = #{place_longitude} " +
        "WHERE id = #{id}")
    void updateCoordinate(PetCoordinateInfoDTO dto);
} 
