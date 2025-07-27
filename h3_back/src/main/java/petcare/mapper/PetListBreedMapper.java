package petcare.mapper;

import petcare.dto.PetListBreedListDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PetListBreedMapper {
    List<PetListBreedListDTO> selectBreedforPetList();
}
