package petcare.mapper;

import petcare.dto.NoticePetprofileDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NoticePetprofileMapper {
    List<NoticePetprofileDTO> selectNoticePetprofile();
}
