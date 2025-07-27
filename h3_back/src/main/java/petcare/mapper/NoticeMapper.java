package petcare.mapper;

import petcare.dto.NewNoticeRegisterDTO;
import petcare.dto.NoticeListDTO;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NoticeMapper {
    int insertNewNotice(NewNoticeRegisterDTO newNoticeRegisterDTO);

    List<NoticeListDTO> selectNoticeList();

    int updateNotice(NoticeListDTO dto);

    int deleteNotice(int id);
}
