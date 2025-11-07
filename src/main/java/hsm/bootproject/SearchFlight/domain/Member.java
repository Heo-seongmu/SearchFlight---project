package hsm.bootproject.SearchFlight.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Member {

	@Id
	@GeneratedValue
	private Long id;
	
	@Column(unique = true) // 일반 로그인용 ID (예: "my_id_123")
	private String loginId;
	
	@Column(unique = true, nullable = true) // 카카오 고유 ID (숫자)
	private Long kakaoId;
	
	@Column(nullable = false)
	private String userName;
	
	@Column(nullable = false)
	private String userPw;
	
	@Column(unique = true, nullable = false)
	private String email;
	
	private String phone;
	
	private String web; // 'email' 또는 'kakao'
	
	public Member() {
	}
}