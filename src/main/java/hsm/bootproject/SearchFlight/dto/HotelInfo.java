package hsm.bootproject.SearchFlight.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class HotelInfo {

    private String hotelId;
    private String name;
    private GeoCode geoCode;
    private HotelDistance distance; // 필드명 'distance'
    private String koreanName;

    // Getters and Setters
    public String getHotelId() { return hotelId; }
    public void setHotelId(String hotelId) { this.hotelId = hotelId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public GeoCode getGeoCode() { return geoCode; }
    public void setGeoCode(GeoCode geoCode) { this.geoCode = geoCode; }

    public HotelDistance getDistance() { return distance; } // Getter
    public void setDistance(HotelDistance distance) { this.distance = distance; } // Setter
}
