package hsm.bootproject.SearchFlight.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bookings")
@Getter
@Setter
public class Booking {

    // ... (id, 가는 편 정보, 종합 정보 등은 그대로) ...

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- 가는 편 항공권 정보 (필수) ---
    @Column(nullable = false)
    private String departureAirline;

    @Column(nullable = false)
    private LocalDateTime departureTime;

    @Column(nullable = false)
    private LocalDateTime departureArrivalTime;

    @Column(nullable = false)
    private String departureOriginCode;

    @Column(nullable = false)
    private String departureDestinationCode;
    
    @Column(nullable = false)
    private String departureKoLocation;

    @Column(nullable = false)
    private String arrivalKoLocation; // '도착지' 한글명은 편도/왕복 공통이므로 필수로 둡니다.

    // --- 오는 편 항공권 정보 (선택) ---
    // 👇 [수정] nullable = false 제거
    @Column 
    private String returnAirline;

    // 👇 [수정] nullable = false 제거
    @Column
    private LocalDateTime returnTime;

    // 👇 [수정] nullable = false 제거
    @Column
    private LocalDateTime returnArrivalTime;

    // 👇 [수정] nullable = false 제거
    @Column
    private String returnOriginCode;

    // 👇 [수정] nullable = false 제거
    @Column
    private String returnDestinationCode;


    // --- 종합 예약 정보 ---
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
    
    // ... (bookingStatus, createdAt, member 등 나머지 필드) ...
    @Column(nullable = false)
    private String bookingStatus;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
}
