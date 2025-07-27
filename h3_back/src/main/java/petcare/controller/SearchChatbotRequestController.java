package petcare.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import petcare.service.SearchChatbotRequestService;
import petcare.dto.SearchChatbotRequestDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/petcare")
public class SearchChatbotRequestController {


    private final SearchChatbotRequestService chatservice;

    public SearchChatbotRequestController(SearchChatbotRequestService chatservice) {
        this.chatservice = chatservice;
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody SearchChatbotRequestDTO chatRequest) {
        return chatservice.fowardToFastApi(chatRequest);
    }

    @PostMapping("/close_chatsession")
    public ResponseEntity<?> closeSession() {
        return chatservice.closeSessionInFastApi();
    }
    
}