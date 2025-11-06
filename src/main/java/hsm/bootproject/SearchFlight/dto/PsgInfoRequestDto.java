package hsm.bootproject.SearchFlight.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PsgInfoRequestDto {

	private String departureKoLocation;
    private String arrivalKoLocation;
    private Integer adults;
    private Integer children;
    private Integer infants;
    private String travelClass;

    private FlightDetailDto departureFlight;
    private FlightDetailDto returnFlight; // 편도일 경우 null
	
}
