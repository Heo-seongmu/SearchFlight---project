package hsm.bootproject.SearchFlight.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import hsm.bootproject.SearchFlight.Service.ChatService;
import hsm.bootproject.SearchFlight.domain.Member; // [추가] Member 도메인
import hsm.bootproject.SearchFlight.dto.chatMessageDto;
import jakarta.servlet.http.HttpSession; // [추가] HttpSession

@RestController
public class ChatHistoryController {

	@Autowired
	private ChatService chatService;
    
    @Autowired
    private HttpSession session; 

	@GetMapping("/chat/history")
    public List<chatMessageDto> getChatHistory() {
        
        Member loginUser = (Member) session.getAttribute("loginUser");

        // 1. (비로그인) loginUser가 세션에 없으면 비로그인 메시지 반환
        if (loginUser == null) {
            return Collections.singletonList(
                new chatMessageDto("안녕하세요! '무성의 여행'입니다. 어떤 여행 스타일을 원하시나요?", "AI")
            );
        }

        // 2. (로그인) DB에서 내역 조회 (loginUser의 userId 사용)
        List<chatMessageDto> history = chatService.getChatHistory(loginUser.getLoginId());

        // 3. (로그인) 내역이 없다면, 환영 메시지 반환
        if (history.isEmpty()) { 
             return Collections.singletonList(
                new chatMessageDto("안녕하세요! '무성의 여행'입니다. 다시 찾아주셨네요. 무엇을 도와드릴까요?", "AI")
            );
        }

        return history;
    }
}
