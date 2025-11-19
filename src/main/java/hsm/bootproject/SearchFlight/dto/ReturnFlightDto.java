package hsm.bootproject.SearchFlight.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReturnFlightDto {

	private String id;
	private String returnCarrierCode;
	private String returnDepartureTime;
	private String returnArrivalTime;
	private boolean isReturnDirectFlight;
	private long returnTotalPrice;
	private String returnDepartureCode; // 오는 편 출발 공항 코드
	private String returnArrivalCode;   // 오는 편 도착 공항 코드
	private String returnDepartureDate; 
    private String returnArrivalDate;
    
    private int returnDayDifference;
}
