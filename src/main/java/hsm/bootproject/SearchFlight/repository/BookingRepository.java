package hsm.bootproject.SearchFlight.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hsm.bootproject.SearchFlight.domain.Booking;
import hsm.bootproject.SearchFlight.domain.Member;

public interface BookingRepository extends JpaRepository<Booking, Long>{

	@Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
	           "FROM Booking b " +
	           "WHERE b.member = :member " +
	           "AND b.departureAirline = :departureAirline " +
	           "AND b.departureTime = :departureTime " +
	           "AND b.returnAirline = :returnAirline " +
	           "AND b.returnTime = :returnTime " +
	           "AND b.bookingStatus <> 'CANCELLED'") // <> 는 '같지 않다'는 의미
	    boolean existsDuplicateBooking(
	        @Param("member") Member member,
	        @Param("departureAirline") String departureAirline,
	        @Param("departureTime") LocalDateTime departureTime,
	        @Param("returnAirline") String returnAirline,
	        @Param("returnTime") LocalDateTime returnTime
	    );

	List<Booking> findByMemberIdAndBookingStatusOrderByCreatedAtDesc(Long memberId, String string); 
	
}
