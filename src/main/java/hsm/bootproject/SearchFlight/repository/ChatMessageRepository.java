package hsm.bootproject.SearchFlight.repository;

import hsm.bootproject.SearchFlight.domain.ChatMessage;
import hsm.bootproject.SearchFlight.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // 특정 회원의 채팅 내역을 시간순으로 정렬하여 모두 조회
    List<ChatMessage> findByMemberOrderByCreatedAtAsc(Member member);
}
