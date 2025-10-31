package hsm.bootproject.SearchFlight.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter@ToString
public class basicArea {

	@Id
	@GeneratedValue
	private Long id;
	
	private String kolocation;
	
	private String enlocation;
	
	private String iataCode;
	
	private String country;
	
	public basicArea() {
		
	}
}
