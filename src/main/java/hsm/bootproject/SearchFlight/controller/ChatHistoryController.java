package hsm.bootproject.SearchFlight.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hsm.bootproject.SearchFlight.Service.ChatService;
import hsm.bootproject.SearchFlight.domain.Member;
import hsm.bootproject.SearchFlight.dto.ChatRoomDto;
import hsm.bootproject.SearchFlight.dto.chatMessageDto;
import jakarta.servlet.http.HttpSession;

@RestController
public class ChatHistoryController {

    @Autowired
    private ChatService chatService;
    
    @Autowired
    private HttpSession session;

    // 1. 채팅방 목록 조회
    @GetMapping("/chat/rooms")
    public List<ChatRoomDto> getChatRooms() {
        Member loginUser = (Member) session.getAttribute("loginUser");
        if (loginUser == null) {
            return Collections.emptyList();
        }
        return chatService.getChatRooms(loginUser.getLoginId());
    }

    // 2. 특정 채팅방 메시지 내역 조회
    // [중요] 여기 @RequestParam("roomId") 부분이 수정되었습니다.
    @GetMapping("/chat/messages")
    public List<chatMessageDto> getRoomMessages(@RequestParam("roomId") Long roomId) {
        return chatService.getMessagesByRoomId(roomId);
    }
}