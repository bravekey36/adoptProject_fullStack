package petcare.mapper;

import petcare.dto.NoticePetfindinfoDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NoticePetfindinfoMapper {
    List<NoticePetfindinfoDTO> selectNoticePetfindinfo();
}
