package hsm.bootproject.SearchFlight.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import hsm.bootproject.SearchFlight.Service.BookingService;
import hsm.bootproject.SearchFlight.domain.Booking;
import hsm.bootproject.SearchFlight.domain.Member;
import hsm.bootproject.SearchFlight.domain.popular;
import hsm.bootproject.SearchFlight.dto.DestinationStatsDto;
import hsm.bootproject.SearchFlight.dto.PopularResponseDto;
import hsm.bootproject.SearchFlight.repository.PopularRepository;
import hsm.bootproject.SearchFlight.repository.SearchLogRepository;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
	
	@Autowired
    private BookingService bookingService;
	
	@Autowired
	private PopularRepository popularRepository;
	
	@Autowired
    private SearchLogRepository searchLogRepository;
		
	@GetMapping("/")
	public String home(Model model) { // 1. Model 파라미터 추가
		
		List<DestinationStatsDto> domesticTrend = searchLogRepository.findTopDestinationsByCountry("국내", PageRequest.of(0, 6));
	    model.addAttribute("domesticTrend", domesticTrend);

	    // 2. [해외] 실시간 인기 검색어 Top 6
	    List<DestinationStatsDto> overseasTrend = searchLogRepository.findTopDestinationsByCountry("해외", PageRequest.of(0, 6));
	    model.addAttribute("overseasTrend", overseasTrend);
		
		// 2. DB에서 모든 여행지 데이터 가져오기
		List<popular> allPopulars = popularRepository.findAll();
		
		// 3. 국내 여행지(isDomestic = true) 필터링
		List<popular> domesticList = allPopulars.stream()
				.filter(p -> Boolean.TRUE.equals(p.getIsDomestic()))
				.collect(Collectors.toList());
		
		// 4. 해외 여행지(isDomestic = false) 필터링
		List<popular> overseasList = allPopulars.stream()
				.filter(p -> Boolean.FALSE.equals(p.getIsDomestic()))
				.collect(Collectors.toList());
		
		// 5. Model에 담아서 HTML로 전달 (키 이름은 HTML의 th:each="dest : ${키이름}" 과 같아야 함)
		model.addAttribute("domesticList", domesticList);
		model.addAttribute("overseasList", overseasList);

		return "main";
	}
	
	@GetMapping("/api/destination/detail")
	@ResponseBody
	public PopularResponseDto getDestinationDetail(@RequestParam("cityName") String cityName) {
		// 도시 이름으로 DB 조회
		popular p = popularRepository.findByCityName(cityName)
				.orElseThrow(() -> new IllegalArgumentException("도시를 찾을 수 없습니다."));

		return new PopularResponseDto(p);
	}
	
	@GetMapping("/chat")
	public String chat() {
		return "chat";
	}
	
	@GetMapping("/main")
	public String main() {
		return "redirect:/";
	}
	@GetMapping("/airList")
	public String test() {
		return "airList";
	}
	
	
	@GetMapping("/revList")
	public String revList(Model model, HttpSession session) {
        // 1. 세션에서 현재 로그인한 사용자 정보를 가져옵니다.
        Member loginUser = (Member) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/member/login"; 
        }
        List<Booking> bookingList = bookingService.getMyBookings(loginUser.getId());
        model.addAttribute("bookingList", bookingList);
        model.addAttribute("viewType", "RESERVED");
		
        // 5. 뷰 템플릿의 이름을 반환합니다.
		return "revList";
	}
	
	@GetMapping("/revCancelList")
    public String revCancelList(Model model, HttpSession session) {
        
        Member loginUser = (Member) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/member/login"; 
        }

        // 1. '취소된(CANCELLED)' 내역만 조회
        List<Booking> bookingList = bookingService.getMyCancelledBookings(loginUser.getId());
        
        // 2. 모델에 데이터 추가
        model.addAttribute("bookingList", bookingList);
        model.addAttribute("viewType", "cancelled"); 

        // 3. ⭐️ 동일한 템플릿 반환
        return "revList";
    }
}
