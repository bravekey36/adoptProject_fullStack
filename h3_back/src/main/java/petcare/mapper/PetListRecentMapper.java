package petcare.mapper;

import petcare.dto.PetListRecentDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PetListRecentMapper {
    List<PetListRecentDTO> selectRecentPetsWithImagesAndShelter();
}
