package hsm.bootproject.SearchFlight.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "search_log_gen")
    @SequenceGenerator(
            name = "search_log_gen",        // 제너레이터 이름 (위의 generator와 일치해야 함)
            sequenceName = "search_log_seq", // DB에 있는 실제 시퀀스 이름 (로그에 뜬 이름)
            allocationSize = 1              // DB 시퀀스 증가값과 1:1로 맞춤
        )
    private Long id; // 로그 고유 번호

    private String iataCode; // 도착지 코드 (NRT)
    
    private String cityName; // 도착지 이름 (도쿄/나리타) - 화면 표시용
    
    private String country;

    @CreationTimestamp // 저장될 때 현재 시간이 자동으로 들어감
    private LocalDateTime searchDate; 
    
    // 생성자 편의상 추가
    public SearchLog(String iataCode, String cityName,String country) {
        this.iataCode = iataCode;
        this.cityName = cityName;
        this.country = country;
    }
}
