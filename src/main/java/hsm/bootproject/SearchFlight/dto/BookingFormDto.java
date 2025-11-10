package hsm.bootproject.SearchFlight.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingFormDto {
    
    // 1. 예약자 정보
    private String bookerName;
    private String bookerPhone;
    private String bookerEmail;

    // 2. 탑승객 정보 (List)
    // 폼의 name="passengers[0].lastName" 속성이
    // 이 'passengers' 리스트의 0번째 항목(PassengerFormDto)의 'lastName' 필드에 자동으로 매핑됩니다.
    private List<PassengerFormDto> passengers;

    // 3. 항공편 정보 (Nested Object)
    // 폼의 name="departureFlight.id" 속성이
    // 이 'departureFlight' 객체(FlightFormDto)의 'id' 필드에 자동으로 매핑됩니다.
    private FlightFormDto departureFlight;
    private FlightFormDto returnFlight; // 왕복이 아닐 경우 null

    // 4. 기타 숨겨진 정보
    private String departureKoLocation;
    private String arrivalKoLocation;
    private int adults;
    private int children;
    private int infants;
    private String travelClass;
    
    private boolean isDomestic;
}