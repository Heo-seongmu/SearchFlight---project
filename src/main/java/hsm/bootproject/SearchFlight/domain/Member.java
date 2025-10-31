package hsm.bootproject.SearchFlight.domain;

import hsm.bootproject.SearchFlight.dto.MemberDto;
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
	
	private String userId;
	
	private String userName;
	
	private String web;
	
	public Member() {
		
	}
	
	public Member(MemberDto memberDto) {
		this.userId = memberDto.getUserId();
		this.userName = memberDto.getUserName();
		this.web = memberDto.getWeb();
	}
	
}
