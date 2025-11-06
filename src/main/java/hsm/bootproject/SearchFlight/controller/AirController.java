package hsm.bootproject.SearchFlight.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import hsm.bootproject.SearchFlight.Service.AirService;
import hsm.bootproject.SearchFlight.Service.BookingService;
import hsm.bootproject.SearchFlight.domain.Member;
import hsm.bootproject.SearchFlight.dto.BookingRequestDto;
import hsm.bootproject.SearchFlight.dto.BookingResponseDto;
import hsm.bootproject.SearchFlight.dto.PsgInfoRequestDto;
import hsm.bootproject.SearchFlight.dto.ReturnFlightDto;
import hsm.bootproject.SearchFlight.dto.airParmDto;
import hsm.bootproject.SearchFlight.dto.airportDto;
import hsm.bootproject.SearchFlight.dto.searchAirDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;

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
	
	@GetMapping("/bookings/cancel/{id}")
    public String cancelBooking(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        
        try {
            bookingService.cancelBookingById(id);
            
            // 성공 메시지를 'resultModal'로 전달
            redirectAttributes.addFlashAttribute("cancelSuccess", "예약이 성공적으로 취소됐습니다!");
            
        } catch (EntityNotFoundException | IllegalStateException e) {
            // 실패 메시지를 'resultModal'로 전달
            redirectAttributes.addFlashAttribute("cancelError", e.getMessage());
            
        } catch (Exception e) {
             redirectAttributes.addFlashAttribute("cancelError", "처리 중 알 수 없는 오류가 발생했습니다.");
        }

        // '나의 예약 내역' 페이지로 리다이렉트
        return "redirect:/revList"; 
    }
	
	@GetMapping("/bookings/rebook/{id}")
    public String rebookBooking(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        
        try {
            // 1. 서비스 호출 (신규 메서드)
            bookingService.rebookBookingById(id);
            
            // 2. 성공 메시지 전달
            redirectAttributes.addFlashAttribute("rebookSuccess", "예약이 '예약 확정' 상태로 변경됐습니다.");
            
        } catch (EntityNotFoundException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("cancelError", e.getMessage()); // (기존 에러 메시지 재활용)
        } catch (Exception e) {
             redirectAttributes.addFlashAttribute("cancelError", "처리 중 알 수 없는 오류가 발생했습니다.");
        }

        // 3. '예약된 내역' 페이지로 리다이렉트
        return "redirect:/revList";
    }
	
	@PostMapping("/PsgInfo")
    public String showPsgInfoPage(PsgInfoRequestDto psgInfoRequestDto, Model model) {
        
        // 1. (기존) DTO를 모델에 추가
        model.addAttribute("bookingInfo", psgInfoRequestDto);
        
        // ▼▼▼ [ ⭐️ 여기가 수정/추가된 부분 ⭐️ ] ▼▼▼
        
        // 2. 국내선/국제선 여부 판별
        boolean isDomesticFlight = false;
        if (psgInfoRequestDto.getDepartureFlight() != null) {
            String originIata = psgInfoRequestDto.getDepartureFlight().getOriginCode();
            String destIata = psgInfoRequestDto.getDepartureFlight().getDestinationCode();
            
            // AirService를 통해 두 공항이 *모두* 국내 공항인지 확인
            if (airService.isDomesticAirport(originIata) && airService.isDomesticAirport(destIata)) {
                isDomesticFlight = true;
            }
        }
        
        // 3. 판별 결과를 "isDomestic" 라는 이름으로 Model에 추가
        model.addAttribute("isDomestic", isDomesticFlight);
        
        // ▲▲▲ [ ⭐️ 여기까지 ⭐️ ] ▲▲▲
        
        // 4. (기존) 로그 출력
        System.out.println("--- PsgInfo 페이지로 전달되는 데이터 ---");
        System.out.println("가는 편: " + psgInfoRequestDto.getDepartureFlight().getId());
        System.out.println("국내선 여부: " + isDomesticFlight); // ⭐️ 확인용 로그 추가
        if (psgInfoRequestDto.getReturnFlight() != null) {
            System.out.println("오는 편: " + psgInfoRequestDto.getReturnFlight().getId());
        }
        System.out.println("승객: 성인 " + psgInfoRequestDto.getAdults());
        System.out.println("------------------------------------");

        // 5. (기존) 뷰 반환
        return "/PsgInfo"; 
    }
	
}
