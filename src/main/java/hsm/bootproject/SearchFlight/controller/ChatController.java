package hsm.bootproject.SearchFlight.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import hsm.bootproject.SearchFlight.Service.ChatService;
import hsm.bootproject.SearchFlight.domain.Member;
import hsm.bootproject.SearchFlight.dto.chatMessageDto;
import lombok.Getter;
import lombok.Setter;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;

    // === DTO 정의 (Inner Class) ===
    
    @Getter
    @Setter
    public static class InboundChatPayload {
        private Long roomId;          // 현재 채팅방 ID (없으면 null)
        private String type;          // 'recommend' 또는 'follow-up'
        private String content;       // 사용자 질문 내용
        private List<chatMessageDto> history; // 비로그인 유저를 위한 대화 기록
    }

    @Getter
    @Setter
    public static class OutboundChatPayload {
        private Long roomId;          // 처리된 채팅방 ID (새로 생성되었을 수 있음)
        private String content;       // AI 응답 내용
        private String sender;        // "AI"

        public OutboundChatPayload(Long roomId, String content, String sender) {
            this.roomId = roomId;
            this.content = content;
            this.sender = sender;
        }
    }

    @MessageMapping("/sendMessage")
    @SendToUser("/queue/reply")
    public OutboundChatPayload sendMessage(@Payload InboundChatPayload payload, SimpMessageHeaderAccessor headerAccessor) throws Exception {
        
        // 1. 세션에서 로그인한 사용자 정보 가져오기
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
            // 2. 서비스 호출
            // ChatService.processMessage 메서드의 파라미터 순서: 
            // (userId, content, clientHistory, roomId, type)
            // 여기서 payload.getHistory()가 빠져서 오류가 났었습니다. 추가했습니다.
            ChatService.ProcessedChatResult result = chatService.processMessage(
                userId,                 // 사용자 ID (String)
                payload.getContent(),   // 메시지 내용 (String)
                payload.getHistory(),   // 클라이언트 측 대화 내역 (List<chatMessageDto>) -> 비로그인 시 사용
                payload.getRoomId(),    // 채팅방 번호 (Long)
                payload.getType()       // 질문 타입 (String)
            );

            // 3. 결과 반환
            // 서비스가 반환한 roomId(새로 생성되었거나 기존 ID)를 프론트로 다시 보내줍니다.
            return new OutboundChatPayload(result.getRoomId(), result.getAiResponse(), "AI");

        } catch (Exception e) {
            e.printStackTrace();
            // 에러 발생 시 처리
            String errorJson = "{\"chat_response\": \"죄송합니다, 서버 내부 오류가 발생했습니다.\"}";
            return new OutboundChatPayload(null, errorJson, "AI");
        }
    }
}