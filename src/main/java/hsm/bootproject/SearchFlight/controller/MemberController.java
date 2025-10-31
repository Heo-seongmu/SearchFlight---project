package hsm.bootproject.SearchFlight.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import hsm.bootproject.SearchFlight.Service.KakaoService;
import hsm.bootproject.SearchFlight.domain.Member;
import hsm.bootproject.SearchFlight.dto.airParmDto;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/member")
public class MemberController {
	
	@Value("${kakao.api.key}")
	private String client_id;
	
	@Value("${kakao.redirect.uri}")
	private String redirect_uri;
		
	@Autowired
	private KakaoService kakaoService;
	
	@Autowired
	private HttpSession session;
	
	@GetMapping("/login")
	public String loginPage() {
		return "login";
	}
	
	@GetMapping("/kakaoLogin")
	public String kakaoLogin() {
		System.out.println("kakao 인가 코드 요청");
		String request_url = "https://kauth.kakao.com/oauth/authorize";
		
		return "redirect:" + request_url + "?client_id=" + client_id + "&redirect_uri=" + redirect_uri
				+ "&response_type=code" + "&prompt=login";
	}
	
	@GetMapping("/kakaoAuthCode")
	public String kakaoAuthCode(@RequestParam("code") String code) {
		System.out.println("임규아");
		Member loginUser = kakaoService.getKakaoUserInfo(code);
		session.setAttribute("loginUser", loginUser);
		// 1. 세션에서 이전에 저장했던 항공권 검색 정보(`airParmDto`)를 가져옵니다.
				airParmDto lastSearchParams = (airParmDto) session.getAttribute("searchParams");
				
				// 2. 만약 저장된 검색 정보가 있다면,
				if (lastSearchParams != null) {
					// 3. 사용한 세션 정보는 깨끗하게 삭제합니다. (다음에 또 사용되는 것을 방지)
					session.removeAttribute("searchParams");
					
					// 4. 저장된 검색 정보를 바탕으로 리다이렉트 URL을 만듭니다.
					//    GET 방식으로 파라미터를 다시 보내 searchAirport를 호출하게 합니다.
					String redirectUrl = UriComponentsBuilder.fromPath("/air/searchAirport")
							.queryParam("tripType", lastSearchParams.getTripType())
							.queryParam("departureCode", lastSearchParams.getDepartureCode())
							.queryParam("arrivalCode", lastSearchParams.getArrivalCode())
							.queryParam("departureDate", lastSearchParams.getDepartureDate())
							.queryParam("returnDate", lastSearchParams.getReturnDate())
							.queryParam("adults", lastSearchParams.getAdults())
							.queryParam("children", lastSearchParams.getChildren())
							.queryParam("infants", lastSearchParams.getInfants())
							.queryParam("travelClass", lastSearchParams.getTravelClass())
							.queryParam("departureKoLocation", lastSearchParams.getDepartureKoLocation())
							.queryParam("arrivalKoLocation", lastSearchParams.getArrivalKoLocation())
							.toUriString();

					return "redirect:" + redirectUrl;
				}
				
				// 5. 저장된 검색 정보가 없다면 (일반적인 로그인 시), 메인 페이지로 보냅니다.
				return "redirect:/main";
			}
	
	@GetMapping("/logout")
	public String logout() {
		session.removeAttribute("loginUser");
		return "redirect:/main";
	}
	
}
