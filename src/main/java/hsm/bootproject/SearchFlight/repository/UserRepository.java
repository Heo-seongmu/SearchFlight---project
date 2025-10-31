package hsm.bootproject.SearchFlight.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import hsm.bootproject.SearchFlight.domain.Member;



public interface UserRepository extends JpaRepository<Member, Long>{

	Member findByUserIdAndUserName(String userId, String userName);

}
