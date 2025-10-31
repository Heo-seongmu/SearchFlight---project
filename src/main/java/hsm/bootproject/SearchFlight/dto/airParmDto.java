package hsm.bootproject.SearchFlight.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class airParmDto {

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
	private String departureKoLocation;
	private String arrivalKoLocation;
	public airParmDto() {
		
	}
	
}




