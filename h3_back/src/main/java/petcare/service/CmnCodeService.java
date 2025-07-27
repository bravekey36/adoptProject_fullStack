package petcare.service;

import petcare.mapper.CmnCodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Map;

@Service
public class CmnCodeService {
	
    @Autowired
    private CmnCodeMapper mapper;
    
    public ArrayList<Map<String, Object>> getCodeList(Map<String, Object> params) {
    	return mapper.getCodeList(params);
    }

    public ArrayList<Map<String, Object>> getShelterList(String sigugun) {
    	return mapper.getShelterList(sigugun == null ? "" : sigugun);
    }

    public int insertCmnCode(Map<String, Object> params) {
        return mapper.insertCmnCode(params);
    }

    public int updateCmnCode(Map<String, Object> params) {
        return mapper.updateCmnCode(params);
    }

    public int deleteCmnCode(Map<String, Object> params) {
        return mapper.deleteCmnCode(params);
    }

    public int deleteCmnCodeGroup(String groupCd) {
        return mapper.deleteCmnCodeGroup(groupCd);
    }
}
