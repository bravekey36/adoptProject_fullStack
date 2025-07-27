package petcare.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import petcare.dto.PetListRandomDTO;
import petcare.dto.PetListUserSelectDTO;

@Mapper
public interface PetListUserSelectMapper {
    List<PetListRandomDTO> searchPets(PetListUserSelectDTO params);
}
