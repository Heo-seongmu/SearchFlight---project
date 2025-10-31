package hsm.bootproject.SearchFlight.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import hsm.bootproject.SearchFlight.Service.AirService;
import hsm.bootproject.SearchFlight.Service.BookingService;
import hsm.bootproject.SearchFlight.domain.Member;
import hsm.bootproject.SearchFlight.dto.BookingRequestDto;
import hsm.bootproject.SearchFlight.dto.BookingResponseDto;
import hsm.bootproject.SearchFlight.dto.ReturnFlightDto;
import hsm.bootproject.SearchFlight.dto.airParmDto;
import hsm.bootproject.SearchFlight.dto.airportDto;
import hsm.bootproject.SearchFlight.dto.searchAirDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/air")
public class AirController {

	@Autowired
	private AirService airService;
	
	@Autowired
	private BookingService bookingService;
	
	@Autowired
	private HttpSession session;
	
	@GetMapping("/searchAir")
	@ResponseBody
	public List<airportDto> searchAir(@RequestParam("text") String text) {
		
		List<airportDto> searchAirports = null;
		try {
			 searchAirports = airService.SearchAirports(text);
			
			
		} catch (IOException e) {
			e.printStackTrace();

		}
		return searchAirports;
	}
	
@RequestMapping(value = "/searchAirport", method = {RequestMethod.GET, RequestMethod.POST})
public String searchAirport(airParmDto airparmDto, Model model, HttpSession session) {
		
		try {
			session.setAttribute("searchParams", airparmDto);
			
			List<searchAirDto> searchairDto = airService.searchAirPort(airparmDto);
			model.addAttribute("searchairDto",searchairDto);
			model.addAttribute("airparmDto",airparmDto);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if ("one-way".equals(airparmDto.getTripType())) {
	        // tripType이 "one-way"이면 oneAirList.html 반환
	        return "oneAirList";
	    } else {
	        // 그 외의 경우 (기본값, "round-trip" 등) airList.html 반환
	        return "airList";
	    }
	}
	
	@GetMapping("/airList")
	public String ari() {
		
		return "redirect:/airList";
	}
	
	@GetMapping("/oneAirList")
	public String oneari() {
		
		return "redirect:/oneAirList";
	}
	
	@PostMapping("/bookings")
	public ResponseEntity<?> createBookingDirect(@RequestBody BookingRequestDto requestDto) {
        try {
            // 👇 [추가] 세션에서 현재 로그인한 사용자 정보를 가져옵니다.
            Member loginUser = (Member) session.getAttribute("loginUser");
            
            // 👇 [추가] 로그인 상태를 확인합니다.
            if (loginUser == null) {
                // 401 Unauthorized: 인증되지 않은 사용자의 요청
                return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
            }

            if (requestDto == null || requestDto.getDepartureFlight() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "필수 항공편 정보가 누락되었습니다."));
            }

            // 👇 [수정] 서비스 호출 시 로그인 사용자 정보를 함께 전달합니다.
            Long bookingId = bookingService.createBookingFromDetails(requestDto, loginUser);
            
            return ResponseEntity.ok(new BookingResponseDto(bookingId, "Booking created successfully."));
        
        } catch (IllegalArgumentException | IllegalStateException e) { // IllegalStateException 처리 추가
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.internalServerError().body(Map.of("message", "예약 생성 중 서버 오류가 발생했습니다."));
        }
    }
	
	@GetMapping("/api/return-flights")
    @ResponseBody
    public List<ReturnFlightDto> getReturnFlights(
            airParmDto airparmDto, 
            @RequestParam("selectedCarrierCode") String selectedCarrierCode, 
            @RequestParam("selectedDepartureTime") String selectedDepartureTime) throws IOException {
        
        // 서비스에 파라미터를 넘겨주고, 오는 편 항공권 목록을 받습니다.
        return airService.findReturnFlights(airparmDto, selectedCarrierCode, selectedDepartureTime); 
    }
	
}
