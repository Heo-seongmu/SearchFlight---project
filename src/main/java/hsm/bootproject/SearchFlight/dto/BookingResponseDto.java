package hsm.bootproject.SearchFlight.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingResponseDto {

	private Long bookingId; 
    private String message;   

  
    public BookingResponseDto(Long bookingId, String message) {
        this.bookingId = bookingId;
        this.message = message;
    }
	
}
