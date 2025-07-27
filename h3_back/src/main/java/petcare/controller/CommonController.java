package petcare.controller;

// import petcare.service.CmnCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import petcare.service.CmnCodeService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// 프론트 서버 실행 주소
@RequestMapping("/api/common")
@RestController
public class CommonController {
    @Autowired
    private CmnCodeService cmnCodeService;
	
    @GetMapping("/codes")
    public ArrayList<Map<String, Object>> getAllCodeList(@RequestParam(name = "useYn", required = false) String useYn) {
        Map<String, Object> params = new HashMap<>();
        params.put("useYn", useYn);
        ArrayList<Map<String, Object>> selectList = cmnCodeService.getCodeList(params);
        // System.out.println(selectList);
        return selectList;
    }

    @GetMapping("/shelters")
    public ArrayList<Map<String, Object>> getShelterList(@RequestParam(name = "sigugun", required = false) String sigugun) {
        ArrayList<Map<String, Object>> selectList = cmnCodeService.getShelterList(sigugun);
        return selectList;
    }

    @PostMapping("/code")
    public ResponseEntity<Map<String, Object>> insertCmnCode(@RequestBody Map<String, Object> params) {
        Map<String, Object> resultMap = new HashMap<>();
        int affectedRows = 0;
        try {
            affectedRows = cmnCodeService.insertCmnCode(params);
        } catch (DuplicateKeyException e) {
            resultMap.put("error", "중복된 코드값이 존재합니다.");
        } catch (Exception e) {
            resultMap.put("error", e.getMessage());
        }
        resultMap.put("success", affectedRows > 0);
        return ResponseEntity.ok(resultMap);
    }

    @PutMapping("/code")
    public ResponseEntity<Map<String, Object>> updateCmnCode(@RequestBody Map<String, Object> params) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success", cmnCodeService.updateCmnCode(params) > 0);
        return ResponseEntity.ok(resultMap);
    }

    @DeleteMapping("/code")
    public ResponseEntity<Map<String, Object>> deleteCmnCode(@RequestBody Map<String, Object> params) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success", cmnCodeService.deleteCmnCode(params) > 0);
        return ResponseEntity.ok(resultMap);
    }

    @DeleteMapping("/code-group")
    public ResponseEntity<Map<String, Object>> deleteCmnCodeGroup(@RequestParam(name = "groupCd", required = true) String groupCd) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success", cmnCodeService.deleteCmnCodeGroup(groupCd) > 0);
        return ResponseEntity.ok(resultMap);
    }

}
