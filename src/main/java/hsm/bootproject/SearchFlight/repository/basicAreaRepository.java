package hsm.bootproject.SearchFlight.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import hsm.bootproject.SearchFlight.domain.basicArea;

public interface basicAreaRepository extends JpaRepository<basicArea, Long>{

	List<basicArea> findByKolocationContainingOrCountryContaining(String kolocation, String country);

}
