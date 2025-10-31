package hsm.bootproject.SearchFlight.dto;

import hsm.bootproject.SearchFlight.domain.Member;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MemberDto {

	private String userId;

	private String userName;

	private String web;
	
	public MemberDto() {
		
	}
	
	public MemberDto(Member member) {
		this.userId = member.getUserId();
		this.userName = member.getUserName();
		this.web = member.getWeb();
	}

}
