package hsm.bootproject.SearchFlight.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import hsm.bootproject.SearchFlight.Service.BookingService;
import hsm.bootproject.SearchFlight.Service.HotelService;
import hsm.bootproject.SearchFlight.domain.Booking;
import hsm.bootproject.SearchFlight.domain.Member;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
	
	@Autowired
    private BookingService bookingService;
	
	@Autowired
	private HotelService hotelService;
	
	@GetMapping("/")
	public String home() {
		return "main";
	}
	
	@GetMapping("/chat")
	public String chat() {
		return "chat";
	}
	
	@GetMapping("/main")
	public String main() {
		return "main";
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
		
        // 5. 뷰 템플릿의 이름을 반환합니다.
		return "revList";
	}
}
