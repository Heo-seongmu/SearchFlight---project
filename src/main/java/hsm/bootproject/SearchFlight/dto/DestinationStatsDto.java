package hsm.bootproject.SearchFlight.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DestinationStatsDto {
    private String iataCode;    // IATA 코드 (CJU)
    private String cityName;    // 도시 이름 (제주)
    //private String country;
    private Long searchCount;   // 검색 횟수 (45)
}
