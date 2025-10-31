package hsm.bootproject.SearchFlight.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import hsm.bootproject.SearchFlight.Service.HotelService;
import hsm.bootproject.SearchFlight.dto.HotelInfo;

@Controller
@RequestMapping("/hotel")
public class HotelController {
	
	@Autowired
	private HotelService hotelService;
	
    @GetMapping("/List")
    public String hotelList(
            @RequestParam("destination") String destination,
            RedirectAttributes redirectAttributes) {
        
        // redirectAttributes에 파라미터를 추가합니다.
        redirectAttributes.addAttribute("destination", destination);

        return "redirect:/hotel/hotelList";
    }

    @GetMapping("/hotelList")
    public String hotelApiList(
            @RequestParam("destination") String destination,
            Model model) {
        
        try {

            List<HotelInfo> hotels = hotelService.hotelList(destination);
            
            model.addAttribute("hotelList", hotels);
            
        } catch (IOException e) {
            e.printStackTrace(); // 콘솔에 에러 출력
            model.addAttribute("errorMessage", "호텔 목록을 조회하는 중 오류가 발생했습니다.");
            model.addAttribute("hotelList", Collections.emptyList()); // 오류 발생 시 빈 리스트 전달
        }ㄴㄴ
        
        return "hotelList";
    }
}