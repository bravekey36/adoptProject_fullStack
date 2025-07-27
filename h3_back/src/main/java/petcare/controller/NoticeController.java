package petcare.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import lombok.RequiredArgsConstructor;

import petcare.dto.NoticePetprofileDTO;
import petcare.dto.NoticePetfindinfoDTO;
import petcare.dto.NewNoticeRegisterDTO;
import petcare.dto.NoticeListDTO;

import petcare.service.NoticePetprofileService;
import petcare.service.NoticePetfindinfoService;
import petcare.service.NoticeService;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://localhost:5173")  // React에서 호출 허용
@RequiredArgsConstructor
public class NoticeController {

    public final NoticePetprofileService noticePetprofileService;
    public final NoticePetfindinfoService noticePetfindinfoService;

    @Autowired
    private NoticeService noticeService;
    
    @GetMapping("/notice/petprofile")
    public List<NoticePetprofileDTO> getPetprofile() {
        return noticePetprofileService.getPetprofiles();
    }

    @GetMapping("/notice/petfindinfo")
    public List<NoticePetfindinfoDTO> getPetfindinfo() {
        return noticePetfindinfoService.getPetfindinfos();
    }

    @PostMapping("/notice/register")
    public String addNewNotice(@RequestBody NewNoticeRegisterDTO newNoticeRegisterDTO) {

        int result = noticeService.saveNewNotice(newNoticeRegisterDTO);
        
        if (result > 0) {
            return "공지사항 저장됨";
        } else {
            return "공지사항 저장 실패";
        }
    }

    @GetMapping("/notice/select")
    public List<NoticeListDTO> selectNotice() {
        return noticeService.getNoticeList();
    }
    
    @PutMapping("/notice/update/{id}")
    public String updateNotice(@PathVariable int id, @RequestBody NewNoticeRegisterDTO dto) {
        return noticeService.updateNotice(id, dto) > 0 ? "공지사항 수정 성공" : "공지사항 수정 실패";
    }

    @DeleteMapping("/notice/delete/{id}")
    public String deleteNotice(@PathVariable int id) {
        return noticeService.deleteNotice(id) > 0 ? "공지사항 삭제 성공" : "공지사항 삭제 실패";
    }

}
