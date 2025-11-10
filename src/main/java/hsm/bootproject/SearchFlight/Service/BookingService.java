package hsm.bootproject.SearchFlight.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hsm.bootproject.SearchFlight.domain.Booking;
import hsm.bootproject.SearchFlight.domain.Member;
import hsm.bootproject.SearchFlight.domain.Passenger;
import hsm.bootproject.SearchFlight.dto.BookingConfirmationDto;
import hsm.bootproject.SearchFlight.dto.BookingFormDto;
import hsm.bootproject.SearchFlight.dto.FlightFormDto;
import hsm.bootproject.SearchFlight.dto.PassengerFormDto;
import hsm.bootproject.SearchFlight.repository.BookingRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class BookingService {

	@Autowired
	private BookingRepository bookingRepository;
	
	@Transactional
    public Booking createBookingFromDetails(BookingConfirmationDto dto, Member member) {
        
        // 1. DTO에서 상세 정보 추출
        BookingFormDto details = dto.getBookingDetails();
        FlightFormDto depFlight = details.getDepartureFlight();
        FlightFormDto retFlight = details.getReturnFlight(); // 편도면 null

        // 2. 데이터 유효성 검사
        if (depFlight == null) {
            throw new IllegalArgumentException("필수 (가는 편) 항공편 정보가 누락되었습니다.");
        }
        if (member == null) {
            throw new IllegalStateException("예약을 위해서는 로그인이 필요합니다.");
        }
        if (details.getPassengers() == null || details.getPassengers().isEmpty()) {
            throw new IllegalArgumentException("필수 탑승객 정보가 누락되었습니다.");
        }

        // 3. [기존 로직] 중복 예약 검사 (편도/왕복 분기 처리)
        boolean isDuplicate;
        if (retFlight != null) {
            // 왕복일 경우
            isDuplicate = bookingRepository.existsDuplicateBooking(
                member,
                depFlight.getCarrierCode(),
                LocalDateTime.parse(depFlight.getDepartureTime()), // String -> LocalDateTime
                retFlight.getCarrierCode(),
                LocalDateTime.parse(retFlight.getDepartureTime())
            );
        } else {
            // 편도일 경우 (오는 편 정보를 null로 전달)
            isDuplicate = bookingRepository.existsDuplicateBooking(
                member,
                depFlight.getCarrierCode(),
                LocalDateTime.parse(depFlight.getDepartureTime()),
                null, // returnCarrierCode
                null  // returnDepartureTime
            );
        }

        if (isDuplicate) {
            throw new IllegalStateException("이미 동일한 항공권 예약이 존재합니다.");
        }

        // --- 4. Booking 엔티티 생성 및 데이터 매핑 ---
        Booking newBooking = new Booking();
        
        newBooking.setMember(member); // 회원 연결
        newBooking.setBookingStatus("RESERVED"); // "예약 완료" 상태
        
        // ⭐️ [수정] 가격은 DTO에 이미 계산된 최종 가격을 사용
        newBooking.setTotalPrice(dto.getFinalTotalPrice());

        // 한글 위치 정보
        newBooking.setDepartureKoLocation(details.getDepartureKoLocation());
        newBooking.setArrivalKoLocation(details.getArrivalKoLocation());

        // 가는 편 정보 (필수)
        newBooking.setDepartureAirline(depFlight.getCarrierCode());
        newBooking.setDepartureOriginCode(depFlight.getOriginCode());
        newBooking.setDepartureDestinationCode(depFlight.getDestinationCode());
        newBooking.setDepartureTime(LocalDateTime.parse(depFlight.getDepartureTime()));
        newBooking.setDepartureArrivalTime(LocalDateTime.parse(depFlight.getArrivalTime()));
        
        // 오는 편 정보 (왕복일 경우에만)
        if (retFlight != null) {
            newBooking.setReturnAirline(retFlight.getCarrierCode());
            newBooking.setReturnOriginCode(retFlight.getOriginCode());
            newBooking.setReturnDestinationCode(retFlight.getDestinationCode());
            newBooking.setReturnTime(LocalDateTime.parse(retFlight.getDepartureTime()));
            newBooking.setReturnArrivalTime(LocalDateTime.parse(retFlight.getArrivalTime()));
        }

        // --- 5. [⭐️ 신규 ⭐️] Passenger 엔티티 목록 생성 ---
        List<Passenger> passengerEntities = new ArrayList<>();
        for (PassengerFormDto paxDto : details.getPassengers()) {
            Passenger passenger = new Passenger();
            passenger.setFirstName(paxDto.getFirstName());
            passenger.setLastName(paxDto.getLastName());
            passenger.setGender(paxDto.getGender());
            passenger.setBirthDate(paxDto.getBirthDate());
            passenger.setPassengerType(paxDto.getPassengerType());
            
            // 국제선 정보 (isDomestic 값에 따라)
            if (!dto.isDomestic()) {
                passenger.setPassportNumber(paxDto.getPassportNumber());
                passenger.setPassportIssuingCountry(paxDto.getPassportIssuingCountry());
                passenger.setPassportExpiryDate(paxDto.getPassportExpiryDate());
            }
            
            // [가장 중요] 연관관계 설정 (Passenger -> Booking)
            passenger.setBooking(newBooking); 
            
            passengerEntities.add(passenger);
        }
        
        // [가장 중요] 연관관계 설정 (Booking -> Passenger)
        newBooking.setPassengers(passengerEntities);

        // --- 6. DB에 저장 ---
        // 1단계에서 Booking 엔티티에 cascade = CascadeType.ALL 설정을 했기 때문에,
        // booking만 저장해도 passengerEntities가 함께 저장됩니다.
        Booking savedBooking = bookingRepository.save(newBooking);
        
        return savedBooking; // 컨트롤러에서 ID와 위치 정보를 사용하기 위해 Booking 객체 반환
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
	    	return bookingRepository.findByMemberIdAndBookingStatusOrderByCreatedAtDesc(memberId, "RESERVED");
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
	        booking.setBookingStatus("RESERVED");
	    }
	
}