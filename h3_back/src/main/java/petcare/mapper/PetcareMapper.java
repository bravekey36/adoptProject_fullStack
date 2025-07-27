package petcare.mapper;

import petcare.dto.PetcareDTO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;


@Mapper
public interface PetcareMapper {
    List<PetcareDTO> getTop10Status();
}
