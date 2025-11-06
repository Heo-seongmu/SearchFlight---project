package hsm.bootproject.SearchFlight.domain;

import jakarta.persistence.Column; // 1. 이 import 문을 꼭 추가해주세요!
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class basicArea {

	@Id
	@GeneratedValue
	private Long id;
	
    // ▼ 2. @Column 어노테이션을 추가하여 DB 컬럼 이름을 명시합니다.
	@Column(name = "KOLOCATION") 
	private String kolocation;
	
    // ▼ 3. @Column 어노테이션 추가
	@Column(name = "ENLOCATION")
	private String enlocation;
	
    // ▼ 4. @Column 어노테이션 추가
	@Column(name = "IATA_CODE")
	private String iataCode;
	
    // ▼ 5. @Column 어노테이션 추가 (가장 중요!)
	@Column(name = "COUNTRY")
	private String country;
 	
	public basicArea() {
		
	}
}