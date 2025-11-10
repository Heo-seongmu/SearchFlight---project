package hsm.bootproject.SearchFlight.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingConfirmationDto {

    // --- 회원 정보 (세션에서 가져옴) ---
    private Long memberId;
    private String memberName; // Member 엔티티의 userName
    private String memberEmail; // Member 엔티티의 email
    
    // --- 예약자 정보 (폼에서 가져옴, 회원 정보와 다를 수 있음) ---
    private String bookerName;
    private String bookerPhone;
    private String bookerEmail;
    
    // --- 항공편 및 탑승객 상세 정보 (폼 DTO 통째로 저장) ---
    private BookingFormDto bookingDetails;
    
    // --- 최종 결제 정보 (서버에서 계산) ---
    private BigDecimal finalTotalPrice;
    
    private boolean isDomestic;
}