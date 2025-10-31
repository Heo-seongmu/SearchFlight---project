package hsm.bootproject.SearchFlight.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hsm.bootproject.SearchFlight.domain.ChatMessage;
import hsm.bootproject.SearchFlight.domain.Member;
import hsm.bootproject.SearchFlight.dto.chatMessageDto;
import hsm.bootproject.SearchFlight.repository.ChatMessageRepository;
import hsm.bootproject.SearchFlight.repository.Memberrepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatRepository; 
    private final Memberrepository memberRepository;
    private final GeminiApiService geminiApiService;

    @Transactional(readOnly = true)
    public List<chatMessageDto> getChatHistory(String userId) {
        // (기존 코드와 동일)
        Member member = memberRepository.findByUserId(userId).orElse(null);
        if (member == null) {
            return new ArrayList<>();
        }

        return chatRepository.findByMemberOrderByCreatedAtAsc(member)
                .stream()
                .map(msg -> new chatMessageDto(
                        msg.getContent(),
                        "USER".equals(msg.getSenderRole()) ? "user" : "AI"
                ))
                .collect(Collectors.toList());
    }

    /**
     * [수정됨] processMessage
     * - 'type' 파라미터를 추가로 받습니다.
     * - 'type'에 따라 'recommend' 또는 'follow-up' API를 선택적으로 호출합니다.
     */
    @Transactional
    public String processMessage(String userId, String userContent, List<chatMessageDto> clientHistory, String type) {
        
        Member member = null;
        List<Map<String, String>> conversationHistoryForGemini = new ArrayList<>();

        // 1. 사용자 식별 (로그인 여부) (기존 코드와 동일)
        if (userId != null) {
            member = memberRepository.findByUserId(userId).orElse(null);
        }

        // 2. Gemini에게 보낼 대화 내역 준비 (기존 코드와 동일)
        if (member != null) {
            // [로그인 사용자]
            saveMessage(member, userContent, "USER");
            conversationHistoryForGemini = chatRepository.findByMemberOrderByCreatedAtAsc(member)
                    .stream()
                    .map(msg -> Map.of(
                            "sender", "USER".equals(msg.getSenderRole()) ? "user" : "AI",
                            "content", msg.getContent()
                    ))
                    .collect(Collectors.toList());
            
        } else {
            // [비로그인 사용자]
            conversationHistoryForGemini = clientHistory.stream()
                    .map(dto -> Map.of(
                            "sender", dto.getSender(), // "user" or "AI"
                            "content", dto.getContent()
                    ))
                    .collect(Collectors.toList());
        }

        // 3. [수정됨] Gemini API 호출 (타입에 따라 분기)
        String aiResponse;
        if ("follow-up".equals(type)) {
            // 후속 질문 모드
            aiResponse = geminiApiService.callFollowUpApi(conversationHistoryForGemini);
        } else {
            // 'recommend' 또는 null (기본값: 추천 모드)
            aiResponse = geminiApiService.callRecommendationApi(conversationHistoryForGemini);
        }


        // 4. (로그인 사용자만) AI 응답을 DB에 저장 (기존 코드와 동일)
        if (member != null) {
            saveMessage(member, aiResponse, "AI");
        }

        // 5. AI 응답 반환 (기존 코드와 동일)
        return aiResponse;
    }

    // (Private Helper) 메시지 저장 (기존 코드와 동일)
    private void saveMessage(Member member, String content, String senderRole) {
        ChatMessage message = new ChatMessage();
        message.setMember(member);
        message.setContent(content);
        message.setSenderRole(senderRole);
        chatRepository.save(message);
    }
}