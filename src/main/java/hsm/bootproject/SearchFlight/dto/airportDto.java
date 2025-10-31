package hsm.bootproject.SearchFlight.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString

public class airportDto {

	private String kolocation;

	private String enlocation;

	private String iataCode;

	private String dataSource;

	public airportDto() {

	}

	public airportDto(String kolocation, String enlocation, String iataCode, String dataSource) {
		this.kolocation = kolocation;
		this.enlocation = enlocation;
		this.iataCode = iataCode;
		this.dataSource = dataSource;

	}
}
