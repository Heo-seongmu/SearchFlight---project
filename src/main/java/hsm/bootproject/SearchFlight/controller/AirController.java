package hsm.bootproject.SearchFlight.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
	public String searchAirport(airParmDto airparmDto, Model model, HttpSession session,
	                            @RequestParam(value = "skipPrice", required = false, defaultValue = "false") boolean skipPrice) {
	    
	    try {
	        session.setAttribute("searchParams", airparmDto);
	        
	        // 1. 메인 리스트 조회 (이건 항상 실행)
	        List<searchAirDto> searchairDto = airService.searchAirPort(airparmDto);
	        model.addAttribute("searchairDto", searchairDto);
	        model.addAttribute("airparmDto", airparmDto);
	        
	        // 2. [수정] skipPrice가 false일 때만 5일치 가격 조회 실행
	        // 페이지 처음 들어올 땐 false라서 실행됨 / 날짜 클릭 시엔 true로 보낼 거라 실행 안 됨
	        if (!skipPrice) {
	            Map<String, String> datePriceMap = airService.getSurroundingPrices(airparmDto);
	            model.addAttribute("datePriceMap", datePriceMap);
	        }

	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    
	    if ("one-way".equals(airparmDto.getTripType())) {
	        return "oneAirList";
	    } else {
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
	
	@PostMapping("/prePsgInfo") 
	public String preProcessPsgInfo(PsgInfoRequestDto psgInfoRequestDto) {
	    // 1. 선택한 항공권 정보를 세션에 "tempBookingInfo"라는 이름으로 저장
	    session.setAttribute("tempBookingInfo", psgInfoRequestDto);
	    
	    // 2. 실제 페이지로 이동해라! (리다이렉트 -> GET 요청 발생)
	    return "redirect:/air/PsgInfo";
	}

	// [1-2] 화면용 (GET): 세션에서 데이터 꺼내서 화면 보여주기
	@GetMapping("/PsgInfo")
	public String showPsgInfoPage(Model model) {
	    // 세션에서 데이터 꺼내기
	    PsgInfoRequestDto dto = (PsgInfoRequestDto) session.getAttribute("tempBookingInfo");
	    
	    // 데이터가 없으면(오류 상황) 메인으로 쫓아냄
	    if (dto == null) {
	        return "redirect:/";
	    }

	    // --- 기존 로직 복사 시작 ---
	    Member loginUser = (Member) session.getAttribute("loginUser");
	    if (loginUser != null) {
	        model.addAttribute("loginUser", loginUser);
	    }

	    model.addAttribute("bookingInfo", dto);

	    // 국내선 여부 판별 로직 (기존 코드 활용)
	    boolean isDomesticFlight = false;
	    if (dto.getDepartureFlight() != null) {
	        String originIata = dto.getDepartureFlight().getOriginCode();
	        String destIata = dto.getDepartureFlight().getDestinationCode();
	        if (airService.isDomesticAirport(originIata) && airService.isDomesticAirport(destIata)) {
	            isDomesticFlight = true;
	        }
	    }
	    model.addAttribute("isDomestic", isDomesticFlight);

	    return "PsgInfo"; 
	}
	
	@PostMapping("/confirmBooking")
	public String processBookingForm(BookingFormDto bookingForm) {
	    Member loginUser = (Member) session.getAttribute("loginUser");
	    if (loginUser == null) {
	        return "redirect:/member/login";
	    }

	    // DTO 변환 로직
	    BookingConfirmationDto confirmationDto = new BookingConfirmationDto();
	    confirmationDto.setMemberId(loginUser.getId());
	    confirmationDto.setMemberName(loginUser.getUserName());
	    confirmationDto.setMemberEmail(loginUser.getEmail());
	    
	    confirmationDto.setBookerName(bookingForm.getBookerName());
	    confirmationDto.setBookerEmail(bookingForm.getBookerEmail());
	    confirmationDto.setBookerPhone(bookingForm.getBookerPhone());
	    
	    confirmationDto.setBookingDetails(bookingForm);
	    confirmationDto.setFinalTotalPrice(bookingForm.getDepartureFlight().getTotalPrice());
	    confirmationDto.setDomestic(bookingForm.isDomestic());

	    // 핵심: 세션에 저장 ("pendingBooking" 이름으로)
	    session.setAttribute("pendingBooking", confirmationDto);

	    // 결제/확인 페이지로 리다이렉트
	    return "redirect:/air/payment";
	}

	// [2-2] 화면용 (GET): 세션 데이터로 화면 그리기
	@GetMapping("/payment")
	public String showPaymentPage(Model model) {
	    // 세션에서 데이터 꺼내기
	    BookingConfirmationDto dto = (BookingConfirmationDto) session.getAttribute("pendingBooking");

	    if (dto == null) {

	        return "redirect:/";
	    }

	    model.addAttribute("confirmationData", dto);

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
            
            //
            
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
