package petcare.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import petcare.dto.PetMissingInfoDTO;
import petcare.service.PetMissingInfoService;

@RestController
@RequestMapping("/petcare")
public class PetMissingInfoController {
    
    @Autowired
	private PetMissingInfoService petMissingInfoService;
	

    @PostMapping("/saveMissingPet")
    public String addMissingPetInfo(@RequestBody PetMissingInfoDTO petMissingInfoDTO) {
        int result = petMissingInfoService.saveMissingPetInfo(petMissingInfoDTO);
        
        if (result > 0) {
            return "발견 신고가 성공적으로 저장되었습니다.";
        } else {
            return "저장 실패";
        }
    }
}
