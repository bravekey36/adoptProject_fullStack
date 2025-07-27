package petcare.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

import petcare.dto.SearchChatbotLogDTO;
import petcare.dto.SearchChatbotLogSelectDTO;
import petcare.dto.SearchChatbotHistoryDTO;

import petcare.service.SearchChatbotLogService;
import petcare.service.SearchChatbotLogSelectService;
import petcare.service.SearchChatbotHistoryService;

@RestController
@RequestMapping("/petcare")
public class SearchChatbotLogController {
    
    @Autowired
    private SearchChatbotLogService searchChatbotLogService;

    @Autowired
    private SearchChatbotLogSelectService searchChatbotLogSelectService;

    @Autowired
    private SearchChatbotHistoryService searchChatbotHistoryService;

    @PostMapping("/chatlog")
    public String addSearchChatbotLog(@RequestBody SearchChatbotLogDTO searchChatbotLogDTO) {
        int result = searchChatbotLogService.savesearchChatbotLog(searchChatbotLogDTO);


        if (result > 0) {
            return "챗봇 로그가 성공적으로 저장되었습니다.";
        } else {
            return "저장 실패";
        }
    }

    @PostMapping("/chatlog/openselect")
    public List<SearchChatbotLogSelectDTO> getSession() {
        return searchChatbotLogSelectService.getSessionList();
    }
    
    @PostMapping("/chatlog/close")
        public String closeSession(@RequestBody Map<String, String> payload) {
        String sessionId = payload.get("sessionId");
        int result = searchChatbotLogService.closeSessionBySessionId(sessionId);
        return result > 0 ? "세션이 성공적으로 종료되었습니다." : "세션 종료 실패 또는 해당 세션 없음";
    }

    @PostMapping("/chatlog/history")
    public ResponseEntity<List<SearchChatbotHistoryDTO>> getChatHistory(@RequestParam("status") String status, @RequestParam("limit") int limit) {

        List<SearchChatbotHistoryDTO> history = searchChatbotHistoryService.getRecentOpenChatLogs(status, limit);

        for (SearchChatbotHistoryDTO dto : history) {
            System.out.println("📌!!!!!!!!!!!!!!! senderType=" + dto.getSenderType() + ", message=" + dto.getMessage());
        }
        
        return ResponseEntity.ok(history);
    }

}
