package hsm.bootproject.SearchFlight.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PassengerFormDto {
    // 공통 정보
    private String lastName;
    private String firstName;
    private String gender;
    private String birthDate; // "YYYY-MM-DD" 형식의 문자열
    private String passengerType; // "ADULT", "CHILD", "INFANT"

    // 국제선 전용 정보 (국내선일 경우 null)
    private String passportNumber;
    private String passportIssuingCountry;
    private String passportExpiryDate;
}