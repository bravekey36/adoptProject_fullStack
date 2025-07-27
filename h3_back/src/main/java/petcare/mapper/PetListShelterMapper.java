package petcare.mapper;

import petcare.dto.PetListShelterListDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PetListShelterMapper {
    List<PetListShelterListDTO> selectShelterforPetList();
}
