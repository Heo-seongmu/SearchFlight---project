package hsm.bootproject.SearchFlight.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// JSON에 있으나 DTO에 정의되지 않은 필드를 무시합니다.
@JsonIgnoreProperties(ignoreUnknown = true)
public class HotelResponse {

    private List<HotelInfo> data;

    // Getters and Setters
    public List<HotelInfo> getData() {
        return data;
    }

    public void setData(List<HotelInfo> data) {
        this.data = data;
    }
}
