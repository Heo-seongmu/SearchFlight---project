package hsm.bootproject.SearchFlight.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingRequestDto {
	private FlightDetailDto departureFlight;
	private FlightDetailDto returnFlight;
	
	private String departureKoLocation;
    private String arrivalKoLocation;
    
    private Integer adults;
    private Integer children;
    private Integer infants;
    private String travelClass;
}
