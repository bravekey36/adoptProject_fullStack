package petcare.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.Map;

@Mapper
public interface CmnCodeMapper {
    ArrayList<Map<String, Object>> getCodeList(Map<String, Object> params);
    ArrayList<Map<String, Object>> getShelterList(@Param("sigugun") String sigugun);
    int insertCmnCode(Map<String, Object> params);
    int updateCmnCode(Map<String, Object> params);
    int deleteCmnCode(Map<String, Object> params);
    int deleteCmnCodeGroup(String groupCd);
}
