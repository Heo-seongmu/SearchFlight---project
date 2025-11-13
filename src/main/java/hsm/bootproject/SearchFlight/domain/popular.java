package hsm.bootproject.SearchFlight.domain;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.type.NumericBooleanConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class popular {

	@Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String cityName; // 도시 이름 (예: 제주, 오사카) - 검색 키

    private String country; // 국가 (예: 대한민국, 일본)

    private String description; // 모달 상단 설명 (예: 천혜의 자연과...)

    private String mainImageUrl; // 메인 페이지 카드에 뜰 이미지 URL

    @Convert(converter = NumericBooleanConverter.class)
    private Boolean isDomestic; // 국내/해외 구분 (true: 국내, false: 해외 

    // 양방향 매핑 (1:N)
    @OneToMany(mappedBy = "popular")
    private List<Activity> activities = new ArrayList<>();
	
}
