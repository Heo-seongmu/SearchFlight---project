package hsm.bootproject.SearchFlight.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlightFormDto {
    private String id;
    private String carrierCode;
    private String originCode;
    private String destinationCode;
    private String departureTime; // "YYYY-MM-DDTHH:MM" 형식의 문자열
    private String arrivalTime;
    private BigDecimal totalPrice; // 이 항공편의 가격
}