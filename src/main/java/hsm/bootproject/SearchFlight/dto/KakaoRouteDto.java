package hsm.bootproject.SearchFlight.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoRouteDto {

	private int duration; // 초 단위
    private int distance; // 미터 단위
    private List<List<Double>> path; // [[lng, lat], [lng, lat], ...]
	
}
