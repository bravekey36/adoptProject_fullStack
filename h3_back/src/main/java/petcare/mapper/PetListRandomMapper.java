package petcare.mapper;

import petcare.dto.PetListRandomDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PetListRandomMapper {
    List<PetListRandomDTO> selectRandomPetList();
}
