package hsm.bootproject.SearchFlight.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlightDetailDto {
	private String id;
    private String carrierCode;
    private String departureTime; // 예: "2025-10-13T16:00"
    private String arrivalTime;   // 예: "2025-10-13T17:40"
    private Double totalPrice;
    private String originCode;
    private String destinationCode;
}
