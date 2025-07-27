package petcare.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import petcare.service.SearchChatbotQueryService;

@RestController
@RequestMapping("/petcare")
@RequiredArgsConstructor
public class SearchChatbotQueryController {
    
    private final SearchChatbotQueryService searchChatbotQueryService;

    @PostMapping("/chat/query")
    public ResponseEntity<?> execute(@RequestBody String sql) {
        try {
            List<Map<String, Object>> result = searchChatbotQueryService.executeRawQuery(sql);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("error", e.getMessage()));
        }
    }
}
