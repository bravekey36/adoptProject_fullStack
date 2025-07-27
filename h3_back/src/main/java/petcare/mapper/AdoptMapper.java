package petcare.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import petcare.dto.AdoptDTO;
import java.util.List;

@Mapper
public interface AdoptMapper {
    List<AdoptDTO> selectByConditions(
        @Param("breed") String breed,
        @Param("weightList") List<String> weightList,
        @Param("gender") String gender,
        @Param("status") String status,
        @Param("neutered") String neutered,
        @Param("features") List<String> features,
        @Param("colorList") List<String> colorList,
        @Param("shelterId") Long shelterId,
        @Param("birthMin") String birthMin,
        @Param("birthMax") String birthMax,
        @Param("limit") int limit
    );
}

