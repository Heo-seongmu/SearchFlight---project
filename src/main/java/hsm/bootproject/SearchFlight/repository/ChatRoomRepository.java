package hsm.bootproject.SearchFlight.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import hsm.bootproject.SearchFlight.domain.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // 사용자의 채팅방 목록을 최신순으로 조회
    List<ChatRoom> findByMemberLoginIdOrderByCreatedAtDesc(String loginId);
}
