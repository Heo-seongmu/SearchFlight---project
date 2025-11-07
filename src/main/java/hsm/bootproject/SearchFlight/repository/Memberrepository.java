package hsm.bootproject.SearchFlight.repository;

import hsm.bootproject.SearchFlight.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface Memberrepository extends JpaRepository<Member, Long> {
	 // [수정됨] 'findByUserId'가 아니라 'findByLoginId' 입니다.
    Optional<Member> findByLoginId(String loginId); 
    
    // [수정됨] 'kakaoId'로 조회합니다.
    Optional<Member> findByKakaoId(Long kakaoId);
    
    // (선택) 이메일 중복 체크용
    Optional<Member> findByEmail(String email); 
}
