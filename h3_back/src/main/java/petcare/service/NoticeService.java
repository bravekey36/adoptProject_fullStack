package petcare.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;

import petcare.dto.NewNoticeRegisterDTO;
import petcare.dto.NoticeListDTO;

import petcare.mapper.NoticeMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeMapper noticeMapper;

    public int saveNewNotice(NewNoticeRegisterDTO newNoticeRegisterDTO) {
        return noticeMapper.insertNewNotice(newNoticeRegisterDTO);
    }

    public List<NoticeListDTO> getNoticeList() {
        return noticeMapper.selectNoticeList();
    }

    public int updateNotice(int id, NewNoticeRegisterDTO dto) {
        NoticeListDTO updateDto = new NoticeListDTO();
        updateDto.setId(id);
        updateDto.setTitle(dto.getTitle());
        updateDto.setContent(dto.getContent());
        return noticeMapper.updateNotice(updateDto);
    }

    public int deleteNotice(int id) {
        return noticeMapper.deleteNotice(id);
    }
    
}
