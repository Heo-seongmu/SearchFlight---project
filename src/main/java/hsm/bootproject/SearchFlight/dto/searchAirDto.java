package hsm.bootproject.SearchFlight.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class searchAirDto {
	
	private String id;
	
	private String tripType; // "round-trip" 또는 "one-way"
	private boolean directFlight; // true 또는 false
	private String departureDate; // "YYYY-MM-DD"
	private String returnDate; // "YYYY-MM-DD"
	private String departureCode; // "ICN"
	private String arrivalCode; // "NRT"
	private String adults; // 1
	private String children; // 0
	private String infants; // 0
	private String travelClass; // "ECONOMY"
	private String numberOfBookableSeats;

	private String departureTime;
	private String arrivalTime;
	private String carrierCode;

	private String totalPrice;
	private long rawTotalPrice;

	private String returnDepartureDate; // 오는 편 출발 날짜
	private String returnDepartureTime; // 오는 편 출발 시간
	private String returnDepartureCode; // 오는 편 출발 공항
	private String returnArrivalTime; // 오는 편 도착 시간
	private String returnArrivalCode; // 오는 편 도착 공항
	private String returnCarrierCode; // 오는 편 항공사 코드
	private boolean isReturnDirectFlight; // 오는 편 직항 여부
	
	private String arrivalDate;
	private String returnArrivalDate;

}
