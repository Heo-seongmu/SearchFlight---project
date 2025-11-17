package hsm.bootproject.SearchFlight.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) 
    private Long id; // 로그 고유 번호

    private String iataCode; // 도착지 코드 (NRT)
    
    private String cityName; // 도착지 이름 (도쿄/나리타) - 화면 표시용

    @CreationTimestamp // 저장될 때 현재 시간이 자동으로 들어감
    private LocalDateTime searchDate; 
    
    // 생성자 편의상 추가
    public SearchLog(String iataCode, String cityName) {
        this.iataCode = iataCode;
        this.cityName = cityName;
    }
}
