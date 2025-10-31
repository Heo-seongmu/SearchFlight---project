package hsm.bootproject.SearchFlight.repository;

import hsm.bootproject.SearchFlight.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface Memberrepository extends JpaRepository<Member, Long> {
    // Spring Security의 Principal.getName()을 기준으로 사용자를 찾기 위해
    Optional<Member> findByUserId(String userId); 
}
