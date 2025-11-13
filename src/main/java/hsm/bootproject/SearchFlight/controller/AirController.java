package hsm.bootproject.SearchFlight.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import hsm.bootproject.SearchFlight.Service.AirService;
import hsm.bootproject.SearchFlight.Service.BookingService;
import hsm.bootproject.SearchFlight.domain.Booking;
import hsm.bootproject.SearchFlight.domain.Member;
import hsm.bootproject.SearchFlight.dto.BookingConfirmationDto;
import hsm.bootproject.SearchFlight.dto.BookingFormDto;
import hsm.bootproject.SearchFlight.dto.PsgInfoRequestDto;
import hsm.bootproject.SearchFlight.dto.ReturnFlightDto;
import hsm.bootproject.SearchFlight.dto.airParmDto;
import hsm.bootproject.SearchFlight.dto.airportDto;
import hsm.bootproject.SearchFlight.dto.searchAirDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
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
	
	@PostMapping("/confirmBooking")
    public String confirmBooking(
            @ModelAttribute BookingFormDto bookingForm, // 1. 폼 데이터가 DTO에 자동 바인딩됨
            // HttpSession session, // (이미 @Autowired 되어 있으므로 파라미터로 안 받아도 됨)
            Model model) {           // 2. 다음 페이지로 데이터 전달할 Model

        Member loginUser = (Member) session.getAttribute("loginUser");

        if (loginUser == null) {
            // 로그인 페이지로 리다이렉트
            return "redirect:/member/login"; 
        }

        BookingConfirmationDto confirmationDto = new BookingConfirmationDto();

        // 3-1. 회원 정보 매핑
        confirmationDto.setMemberId(loginUser.getId());
        confirmationDto.setMemberName(loginUser.getUserName());
        confirmationDto.setMemberEmail(loginUser.getEmail());

        // 3-2. 예약자 정보 매핑 (폼에서 받은 값)
        confirmationDto.setBookerName(bookingForm.getBookerName());
        confirmationDto.setBookerEmail(bookingForm.getBookerEmail());
        confirmationDto.setBookerPhone(bookingForm.getBookerPhone());

        // 3-3. 항공/탑승객 상세 정보 매핑 (폼 DTO 통째로 전달)
        confirmationDto.setBookingDetails(bookingForm);

        // 3-4. 최종 결제 금액 계산
        BigDecimal totalPrice = bookingForm.getDepartureFlight().getTotalPrice();

        confirmationDto.setFinalTotalPrice(totalPrice);
        confirmationDto.setDomestic(bookingForm.isDomestic());
        session.setAttribute("pendingBooking", confirmationDto);
        model.addAttribute("confirmationData", confirmationDto);

        return "revPage"; 
    }

	@PostMapping("/processPayment")
    @ResponseBody //
    public ResponseEntity<Map<String, Object>> processPayment() {
        
        Map<String, Object> response = new HashMap<>();

        try {
            // --- 1. 데이터 검증 ---
            Member loginUser = (Member) session.getAttribute("loginUser");
            if (loginUser == null) {
                throw new IllegalStateException("로그인이 만료되었습니다. 다시 로그인해주세요.");
            }
            
            BookingConfirmationDto confirmationDto = 
                    (BookingConfirmationDto) session.getAttribute("pendingBooking");
            if (confirmationDto == null) {
                throw new IllegalStateException("예약 정보가 만료되었습니다. 항공편 검색부터 다시 시도해주세요.");
            }

            // --- 2. DB에 저장 (핵심 로직) ---
            Booking savedBooking = bookingService.createBookingFromDetails(confirmationDto, loginUser);

            // --- 3. 세션 정리 ---
            session.removeAttribute("pendingBooking");

            // --- 4. 성공 JSON 응답 반환 ---
            response.put("success", true);
            response.put("message", "예약이 성공적으로 완료되었습니다.");
            response.put("bookingId", savedBooking.getId());
            response.put("arrivalKoLocation", confirmationDto.getBookingDetails().getArrivalKoLocation());
            response.put("arrivalAirportCode", confirmationDto.getBookingDetails().getDepartureFlight().getDestinationCode());
            
            return ResponseEntity.ok(response); // 200 OK (성공)

        } catch (Exception e) {
            // --- 5. 모든 종류의 오류 발생 시 ---
            e.printStackTrace(); // 서버 로그에 오류 기록
            
            response.put("success", false);
            // e.getMessage()를 사용해 자바스크립트 alert/modal에 오류 원인을 전달
            response.put("message", "예약 처리 중 오류가 발생했습니다: " + e.getMessage()); 
            
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // 500 Error
        }
    }
	
}
