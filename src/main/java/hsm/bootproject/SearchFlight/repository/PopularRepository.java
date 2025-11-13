package hsm.bootproject.SearchFlight.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import hsm.bootproject.SearchFlight.domain.popular;

public interface PopularRepository extends JpaRepository<popular, Long>{
	Optional<popular> findByCityName(String cityName);
}
