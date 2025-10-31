package hsm.bootproject.SearchFlight.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HotelSentimentData {

    private String hotelId;
    private Integer numberOfReviews;
    private Integer overallRating;
    // sentiments 필드 등은 필요하면 추가

    public String getHotelId() {
        return hotelId;
    }
    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public Integer getNumberOfReviews() {
        return numberOfReviews;
    }
    public void setNumberOfReviews(Integer numberOfReviews) {
        this.numberOfReviews = numberOfReviews;
    }

    public Integer getOverallRating() {
        return overallRating;
    }
    public void setOverallRating(Integer overallRating) {
        this.overallRating = overallRating;
    }
}
