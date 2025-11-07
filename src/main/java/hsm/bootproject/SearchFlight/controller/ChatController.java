package hsm.bootproject.SearchFlight.controller; // 패키지명은 그대로 둡니다.

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor; 
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; 
import com.fasterxml.jackson.databind.ObjectMapper; 

import hsm.bootproject.SearchFlight.Service.ChatService;
import hsm.bootproject.SearchFlight.domain.Member; 
import hsm.bootproject.SearchFlight.dto.chatMessageDto;
import lombok.Getter;
import lombok.Setter;

@Controller
public class ChatController {
	
	
	@Autowired
	private ChatService chatService; 
	
	@Autowired
	private ObjectMapper objectMapper; 

	@Autowired // 생성자 주입 방식
	public ChatController(ChatService chatService, ObjectMapper objectMapper) {
		this.chatService = chatService;
		this.objectMapper = objectMapper;
	}
	
	@Getter @Setter
    public static class InboundChatPayload {
        private String type; // [수정됨] 'recommend' or 'follow-up'
        private String content; 
        private List<chatMessageDto> history;
    }

    @Getter @Setter
    public static class OutboundChatPayload {
        private String content;
        private String sender;
        public OutboundChatPayload(String content, String sender) {
            this.content = content;
            this.sender = sender;
        }
    }

	@Getter @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeminiJsonResponse {
        // (이 DTO는 참고용으로 유지)
        private String city;
        private String country;
        private String reason;
        private List<String> activities;
        private String chat_response;
        
        public GeminiJsonResponse() {} // 기본 생성자
    }


    @MessageMapping("/sendMessage")
    @SendToUser("/queue/reply")
    public OutboundChatPayload sendMessage(@Payload InboundChatPayload payload, SimpMessageHeaderAccessor headerAccessor) throws Exception {
        
        // 1. WebSocket 세션에서 사용자 ID 가져오기 (기존 로직 유지)
        String userId = null;
        try {
            Member loginUser = (Member) headerAccessor.getSessionAttributes().get("loginUser");
            if (loginUser != null) {
                userId = loginUser.getLoginId();
            }
        } catch (Exception e) {
            System.err.println("WebSocket 세션에서 사용자 정보를 가져오지 못했습니다: " + e.getMessage());
        }

        try {
            // 2. [수정됨] ChatService 호출 시 payload.getType() 전달
            String geminiRawResponse = chatService.processMessage(
                userId, 
                payload.getContent(), 
                payload.getHistory(),
                payload.getType() // <-- [수정] type 전달
            );

            // 3. JSON 원본 문자열을 그대로 클라이언트로 전송 (기존 로직 유지)
            return new OutboundChatPayload(geminiRawResponse, "AI");

        } catch (Exception e) {
            e.printStackTrace();
            // 5. 컨트롤러 레벨의 에러 발생 시 (기존 로직 유지)
            String errorJson = "{\"chat_response\": \"죄송합니다, 서버 내부 오류가 발생했습니다.\"}";
            return new OutboundChatPayload(errorJson, "AI");
        }
    }
}