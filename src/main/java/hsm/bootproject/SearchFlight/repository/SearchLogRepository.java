package hsm.bootproject.SearchFlight.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import hsm.bootproject.SearchFlight.domain.SearchLog;
import hsm.bootproject.SearchFlight.dto.DestinationStatsDto;

@Repository
public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {

	@Query("SELECT new hsm.bootproject.SearchFlight.dto.DestinationStatsDto(s.iataCode, s.cityName, COUNT(s)) " +
	           "FROM SearchLog s " +
	           "WHERE s.country = :country " +  // 국가 필터링 조건 추가
	           "GROUP BY s.iataCode, s.cityName " +
	           "ORDER BY COUNT(s) DESC")
	    List<DestinationStatsDto> findTopDestinationsByCountry(@Param("country") String country, Pageable pageable);
	}

