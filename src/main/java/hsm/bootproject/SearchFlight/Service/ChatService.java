package hsm.bootproject.SearchFlight.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hsm.bootproject.SearchFlight.domain.ChatMessage;
import hsm.bootproject.SearchFlight.domain.ChatRoom;
import hsm.bootproject.SearchFlight.domain.Member;
import hsm.bootproject.SearchFlight.dto.ChatRoomDto;
import hsm.bootproject.SearchFlight.dto.chatMessageDto;
import hsm.bootproject.SearchFlight.repository.ChatMessageRepository;
import hsm.bootproject.SearchFlight.repository.ChatRoomRepository;
import hsm.bootproject.SearchFlight.repository.Memberrepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    // [중요] final 키워드가 붙은 필드는 반드시 생성자 주입(@RequiredArgsConstructor)을 통해 초기화되어야 합니다.
    private final ChatMessageRepository chatRepository; 
    private final Memberrepository memberRepository;
    private final ChatRoomRepository chatRoomRepository; 
    private final GeminiApiService geminiApiService;

    // 1. 사이드바용 채팅방 목록 조회 (날짜 포맷 적용)
    @Transactional(readOnly = true)
    public List<ChatRoomDto> getChatRooms(String loginId) {
        return chatRoomRepository.findByMemberLoginIdOrderByCreatedAtDesc(loginId)
                .stream()
                .map(room -> new ChatRoomDto(
                        room.getId(),
                        room.getTitle(),
                        // 날짜 포맷 (예: 2025-12-05 14:30)
                        room.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                ))
                .collect(Collectors.toList());
    }

    // 2. 특정 채팅방의 메시지 내역 조회 (클릭 시)
    @Transactional(readOnly = true)
    public List<chatMessageDto> getMessagesByRoomId(Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));
        
        return room.getMessages().stream()
                .map(msg -> new chatMessageDto(
                        msg.getContent(),
                        "USER".equals(msg.getSenderRole()) ? "user" : "AI"
                ))
                .collect(Collectors.toList());
    }

    // 3. (구버전 호환용) 전체 채팅 내역 조회
    @Transactional(readOnly = true)
    public List<chatMessageDto> getChatHistory(String userId) {
        Member member = memberRepository.findByLoginId(userId).orElse(null);
        if (member == null) return new ArrayList<>();

        return chatRepository.findByMemberOrderByCreatedAtAsc(member)
                .stream()
                .map(msg -> new chatMessageDto(
                        msg.getContent(),
                        "USER".equals(msg.getSenderRole()) ? "user" : "AI"
                ))
                .collect(Collectors.toList());
    }

    /**
     * 핵심 로직: 메시지 처리 (방 생성/조회 -> 메시지 저장 -> AI 호출 -> 응답 저장)
     */
    @Transactional
    public ProcessedChatResult processMessage(String userId, String userContent, List<chatMessageDto> clientHistory, Long roomId, String type) {
        
        Member member = null;
        ChatRoom chatRoom = null;
        
        // AI에게 보낼 대화 내역 (수정 가능한 리스트로 생성)
        List<Map<String, String>> conversationHistoryForGemini = new ArrayList<>();

        // A. 사용자 식별
        if (userId != null) {
            member = memberRepository.findByLoginId(userId).orElse(null);
        }

        // B. [로그인 사용자] 채팅방 로직 및 메시지 저장
        if (member != null) {
            // 1. 방이 없으면 생성 (제목 추출 포함), 있으면 조회
            if (roomId == null) {
                String title = extractTitleFromContent(userContent);
                chatRoom = ChatRoom.create(member, title);
                chatRoomRepository.save(chatRoom);
            } else {
                chatRoom = chatRoomRepository.findById(roomId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방"));
            }

            // 2. 사용자 메시지 DB 저장
            // (주의: 여기서 저장해도 chatRoom.getMessages() 리스트에는 즉시 반영되지 않을 수 있음)
            saveMessage(member, chatRoom, userContent, "USER");

            // 3. Gemini에게 보낼 문맥 구성 (DB에 저장된 이전 내역 가져오기)
            conversationHistoryForGemini = chatRoom.getMessages().stream()
                    .map(msg -> Map.of(
                            "sender", "USER".equals(msg.getSenderRole()) ? "user" : "AI",
                            "content", msg.getContent()
                    ))
                    .collect(Collectors.toList());
            
            // [!!! 핵심 수정 부분 !!!] 
            // DB 조회 리스트에 방금 입력한 질문이 포함되지 않았을 경우를 대비해
            // 현재 사용자의 질문을 리스트 마지막에 강제로 추가합니다.
            conversationHistoryForGemini.add(Map.of("sender", "user", "content", userContent));
            
        } else {
            // C. [비로그인 사용자] (클라이언트 기록 의존)
            if (clientHistory != null) {
                conversationHistoryForGemini = clientHistory.stream()
                        .map(dto -> Map.of(
                                "sender", dto.getSender(),
                                "content", dto.getContent()
                        ))
                        .collect(Collectors.toList());
            }
            // 현재 메시지도 추가
            conversationHistoryForGemini.add(Map.of("sender", "user", "content", userContent));
        }

        // D. Gemini API 호출
        String aiResponse;
        if ("follow-up".equals(type)) {
            aiResponse = geminiApiService.callFollowUpApi(conversationHistoryForGemini);
        } else {
            aiResponse = geminiApiService.callRecommendationApi(conversationHistoryForGemini);
        }

        // E. [로그인 사용자] AI 응답 저장
        if (member != null && chatRoom != null) {
            saveMessage(member, chatRoom, aiResponse, "AI");
        }

        // F. 결과 반환 (방 ID와 응답 내용)
        Long currentRoomId = (chatRoom != null) ? chatRoom.getId() : null;
        return new ProcessedChatResult(currentRoomId, aiResponse);
    }

    // (Private Helper) 메시지 저장
    private void saveMessage(Member member, ChatRoom chatRoom, String content, String senderRole) {
        ChatMessage message = new ChatMessage();
        message.setMember(member);
        message.setChatRoom(chatRoom); 
        message.setContent(content);
        message.setSenderRole(senderRole);
        chatRepository.save(message);
    }

    // (Private Helper) 채팅방 제목 추출 로직
    private String extractTitleFromContent(String content) {
        try {
            // "지역: 값" 과 "테마: 값"을 찾음
            String region = "미정";
            String theme = "테마";

            // 정규식으로 파싱 (콤마나 줄바꿈 전까지 추출)
            Matcher regionMatcher = Pattern.compile("지역:\\s*([^,\n]+)").matcher(content);
            if (regionMatcher.find()) {
                region = regionMatcher.group(1).trim();
            }

            Matcher themeMatcher = Pattern.compile("테마:\\s*([^,\n]+)").matcher(content);
            if (themeMatcher.find()) {
                theme = themeMatcher.group(1).trim();
            }

            // 제목 조합: "아시아 - 힐링"
            if (!"미정".equals(region)) {
                return region + " - " + theme;
            } else {
                // 파싱 실패 시(일반 대화) 앞 15글자 사용
                return content.length() > 15 ? content.substring(0, 15) + "..." : content;
            }
        } catch (Exception e) {
            return "새로운 여행 계획";
        }
    }
    
    // 결과를 리턴하기 위한 내부 클래스
    @Getter
    @AllArgsConstructor
    public static class ProcessedChatResult {
        private Long roomId;
        private String aiResponse;
    }
}