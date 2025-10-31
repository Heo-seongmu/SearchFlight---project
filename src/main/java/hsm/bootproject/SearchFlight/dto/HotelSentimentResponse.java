package hsm.bootproject.SearchFlight.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HotelSentimentResponse {

    private List<HotelSentimentData> data;

    public List<HotelSentimentData> getData() {
        return data;
    }
    public void setData(List<HotelSentimentData> data) {
        this.data = data;
    }
}
