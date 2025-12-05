package hsm.bootproject.SearchFlight.domain;


import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 비로그인 사용(guest)의 경우 member가 null이 됩니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id") 
    private Member member;

    @Column(nullable = false)
    private String senderRole; // "USER" 또는 "AI"

    @Lob 
    @Column(nullable = false) 
    private String content;

    @CreationTimestamp
    private Instant createdAt; // 메시지 생성 시간 (정렬에 사용)
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;
}
