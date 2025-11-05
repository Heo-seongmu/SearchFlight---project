package hsm.bootproject.SearchFlight.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hsm.bootproject.SearchFlight.domain.Booking;
import hsm.bootproject.SearchFlight.domain.Member;
import hsm.bootproject.SearchFlight.dto.BookingRequestDto;
import hsm.bootproject.SearchFlight.dto.FlightDetailDto;
import hsm.bootproject.SearchFlight.repository.BookingRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class BookingService {

	@Autowired
	private BookingRepository bookingRepository;
	
	@Transactional
    public Long createBookingFromDetails(BookingRequestDto requestDto, Member member) {
        
        // 1. DTO에서 가는 편, 오는 편 정보 추출
        FlightDetailDto departureFlightDto = requestDto.getDepartureFlight();
        FlightDetailDto returnFlightDto = requestDto.getReturnFlight(); // 편도면 null

        // 2. 데이터 유효성 검사 (수정됨: 가는 편만 필수)
        if (departureFlightDto == null) {
            throw new IllegalArgumentException("필수 항공편 정보가 누락되었습니다.");
        }
        if (member == null) {
            throw new IllegalStateException("예약을 위해서는 로그인이 필요합니다.");
        }
        
        // --- [수정] 중복 검사 로직 (편도/왕복 분기 처리) ---
        boolean isDuplicate;
        if (returnFlightDto != null) {
            // 왕복일 경우
            isDuplicate = bookingRepository.existsDuplicateBooking(
                member,
                departureFlightDto.getCarrierCode(),
                parseDateTime(departureFlightDto.getDepartureTime()),
                returnFlightDto.getCarrierCode(),
                parseDateTime(returnFlightDto.getDepartureTime())
            );
        } else {
            // 편도일 경우 (오는 편 정보를 null로 전달)
            isDuplicate = bookingRepository.existsDuplicateBooking(
                member,
                departureFlightDto.getCarrierCode(),
                parseDateTime(departureFlightDto.getDepartureTime()),
                null, // returnCarrierCode
                null  // returnDepartureTime
            );
        }

        if (isDuplicate) {
            // 중복된 예약이 있으면 예외를 발생시켜 저장을 막습니다.
            throw new IllegalStateException("이미 동일한 항공권 예약이 존재합니다.");
        }
                
        // 3. Booking 엔티티 생성 및 기본 정보 설정
        Booking newBooking = new Booking();
        
        newBooking.setDepartureKoLocation(requestDto.getDepartureKoLocation());
        newBooking.setArrivalKoLocation(requestDto.getArrivalKoLocation());
        newBooking.setMember(member);
        newBooking.setBookingStatus("CONFIRMED"); // 초기 상태 설정

        // --- 가는 편 정보 설정 (필수) ---
        newBooking.setDepartureAirline(departureFlightDto.getCarrierCode());
        newBooking.setDepartureOriginCode(departureFlightDto.getOriginCode());
        newBooking.setDepartureDestinationCode(departureFlightDto.getDestinationCode());
        newBooking.setDepartureTime(parseDateTime(departureFlightDto.getDepartureTime()));
        newBooking.setDepartureArrivalTime(parseDateTime(departureFlightDto.getArrivalTime()));

        // --- [수정] 가격 계산 (편도/왕복 분기 처리) ---
        BigDecimal totalPrice = BigDecimal.valueOf(departureFlightDto.getTotalPrice());

        // --- [수정] 오는 편 정보 설정 (왕복일 경우에만) ---
        if (returnFlightDto != null) {
            // 왕복일 경우
            newBooking.setReturnAirline(returnFlightDto.getCarrierCode());
            newBooking.setReturnOriginCode(returnFlightDto.getOriginCode());
            newBooking.setReturnDestinationCode(returnFlightDto.getDestinationCode());
            newBooking.setReturnTime(parseDateTime(returnFlightDto.getDepartureTime()));
            newBooking.setReturnArrivalTime(parseDateTime(returnFlightDto.getArrivalTime()));
            
            // 왕복 가격 합산
            //totalPrice = totalPrice.add(BigDecimal.valueOf(returnFlightDto.getTotalPrice()));
        }
        // (편도일 경우 오는 편 정보는 null로 유지됩니다 - 엔티티에서 nullable=true로 설정했음)

        newBooking.setTotalPrice(totalPrice); // 최종 가격 설정

        // 4. 리포지토리를 통해 엔티티를 데이터베이스에 저장
        Booking savedBooking = bookingRepository.save(newBooking);

        // 5. 저장된 예약의 ID를 반환
        return savedBooking.getId();
    }
	    
	    private LocalDateTime parseDateTime(String dateTimeStr) {
	        // 실제 프론트엔드에서 넘어오는 데이터 형식에 맞춰 DateTimeFormatter를 정의해야 합니다.
	        // 여기서는 ISO_LOCAL_DATE_TIME 형식을 가정합니다. (예: "YYYY-MM-DDTHH:MM:SS")
	        try {
	             return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	        } catch (Exception e) {
	            // 형식이 맞지 않을 경우 예외 처리 또는 다른 형식 시도
	            // 예: 날짜와 시간이 분리되어 있다면 조합하는 로직 필요
	            throw new IllegalArgumentException("잘못된 날짜/시간 형식입니다: " + dateTimeStr);
	        }
	    }
	    
	    public List<Booking> getMyBookings(Long memberId) {
	        // 리포지토리를 호출하여 DB에서 예약 목록을 가져와 그대로 반환합니다.
	    	return bookingRepository.findByMemberIdAndBookingStatusOrderByCreatedAtDesc(memberId, "CONFIRMED");
	    }
	    
	    public List<Booking> getMyCancelledBookings(Long memberId) {
	        // 리포지토리에 "CANCELLED" 상태를 넘겨서 조회합니다.
	        return bookingRepository.findByMemberIdAndBookingStatusOrderByCreatedAtDesc(memberId, "CANCELLED");
	    }
	    
	    @Transactional
	    public void cancelBookingById(Long bookingId) {
	        
	        Booking booking = bookingRepository.findById(bookingId)
	            .orElseThrow(() -> new EntityNotFoundException("해당 예약을 찾을 수 없습니다."));

	        if ("CANCELLED".equals(booking.getBookingStatus())) {
	            throw new IllegalStateException("이미 취소된 예약입니다.");
	        }
	        
	        booking.setBookingStatus("CANCELLED"); // ⬅️ 상태 변경
	    }
	    
	    @Transactional
	    public void rebookBookingById(Long bookingId) {
	        
	        // 1. 예약을 찾습니다.
	        Booking booking = bookingRepository.findById(bookingId)
	            .orElseThrow(() -> new EntityNotFoundException("해당 예약을 찾을 수 없습니다."));

	        // 2. 이미 '예약 확정' 상태인지 확인합니다.
	        if ("CONFIRMED".equals(booking.getBookingStatus())) {
	            throw new IllegalStateException("이미 '예약 확정' 상태인 예약입니다.");
	        }
	        
	        // 3. 상태를 "CONFIRMED"로 변경합니다.
	        booking.setBookingStatus("CONFIRMED");
	    }
	
}