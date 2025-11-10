package hsm.bootproject.SearchFlight.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder; // [필수]
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.JsonObject;

import hsm.bootproject.SearchFlight.Service.KakaoService;
import hsm.bootproject.SearchFlight.domain.Member;
import hsm.bootproject.SearchFlight.dto.airParmDto;
import hsm.bootproject.SearchFlight.repository.Memberrepository;
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
	private Memberrepository memberRepository;
	
	@Autowired
	private HttpSession session;
		
	@Autowired
	private PasswordEncoder passwordEncoder; 
	
	@GetMapping("/login")
	public String loginPage() {
		return "login";
	}
	
	/**
	 * [수정됨]
	 * 이메일(아이디)/비밀번호 기반 일반 로그인 처리
	 */
	@PostMapping("/login")
	public String loginProcess(@RequestParam("loginId") String loginId, // login.html의 name="loginId"
							   @RequestParam("password") String password) {
		
		System.out.println("일반 로그인 시도: " + loginId);
		
		// 1. DB에서 'loginId'로 사용자를 조회합니다.
		Optional<Member> memberOptional = memberRepository.findByLoginId(loginId);
		
		if (memberOptional.isPresent()) {
			// 2. 사용자가 존재하면, 비밀번호를 비교합니다.
			Member member = memberOptional.get();
			
			// [중요] passwordEncoder.matches(평문, 암호화된값)로 비교
			if (passwordEncoder.matches(password, member.getUserPw())) {
				// 3. 비밀번호 일치! -> 로그인 성공
				System.out.println("로그인 성공: " + member.getLoginId());
				session.setAttribute("loginUser", member);
				
				// (카카오 로그인과 동일) 항공권 검색 정보가 있었는지 확인
				airParmDto lastSearchParams = (airParmDto) session.getAttribute("searchParams");
				if (lastSearchParams != null) {
					session.removeAttribute("searchParams");

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
				return "redirect:/main"; // 항공권 검색 정보 없으면 메인으로
				
			} else {
				// 3. 비밀번호 불일치
				System.out.println("로그인 실패: 비밀번호 불일치");
				return "redirect:/member/login?error=true";
			}
			
		} else {
			// 2. 사용자가 존재하지 않음
			System.out.println("로그인 실패: 존재하지 않는 아이디");
			return "redirect:/member/login?error=true";
		}
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
		System.out.println("카카오 인증 콜백 수신: " + code);
		
		// 1. 서비스에서 프로필 정보(Json)를 가져옵니다.
		JsonObject kakaoProfile = kakaoService.getKakaoUserProfile(code);
		
		if (kakaoProfile == null) {
			System.out.println("카카오 프로필 정보를 가져오는데 실패했습니다.");
			return "redirect:/member/login?error=true";
		}
		
		// 2. 프로필에서 kakaoId (고유 ID)를 [Long] 타입으로 추출합니다.
		Long kakaoId = kakaoProfile.get("id").getAsLong();
		
		// 3. DB에서 'kakaoId'로 사용자를 조회합니다.
		Optional<Member> existingMemberOptional = memberRepository.findByKakaoId(kakaoId);
		
		if (existingMemberOptional.isPresent()) {
			// --- [ 4. 기존 유저일 경우 ] ---
			Member existingMember = existingMemberOptional.get();
			System.out.println("기존 회원(카카오) 확인. 로그인 처리합니다. ID: " + existingMember.getLoginId());
			
			// 4-1. 세션에 로그인 정보를 저장합니다.
			session.setAttribute("loginUser", existingMember);
			
			// 4-2. 항공권 검색 정보가 있었는지 확인하고 리다이렉트
			airParmDto lastSearchParams = (airParmDto) session.getAttribute("searchParams");
			if (lastSearchParams != null) {
				session.removeAttribute("searchParams");
				// ... (항공권 검색 페이지로 리다이렉트) ...
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
			
			return "redirect:/main";
			
		} else {
			// --- [ 5. 신규 유저일 경우 ] ---
			System.out.println("신규 회원(카카오) 확인. 회원가입 페이지(/member/join)로 리다이렉트합니다.");
			
			// 5-1. 프로필에서 이메일과 닉네임(이름)을 추출합니다. (null 체크 필수)
			String email = "";
			String nickname = "";
			
			if (kakaoProfile.has("kakao_account")) {
				JsonObject kakaoAccount = kakaoProfile.getAsJsonObject("kakao_account");
				if (kakaoAccount.has("email")) {
					email = kakaoAccount.get("email").getAsString();
				}
			}
			if (kakaoProfile.has("properties")) {
				JsonObject properties = kakaoProfile.getAsJsonObject("properties");
				if (properties.has("nickname")) {
					nickname = properties.get("nickname").getAsString();
				}
			}
			
			// 5-2. 회원가입 페이지(join.html)로 리다이렉트할 URL을 만듭니다.
			String redirectUrl = UriComponentsBuilder.fromPath("/member/join")
					.queryParam("email", email)       // 예: test@kakao.com
					.queryParam("name", nickname)     // 예: 카카오테스트
					.queryParam("kakaoId", kakaoId)   // 예: 123456789
					.toUriString();
			
			return "redirect:" + redirectUrl;
		}
	}
	
	@GetMapping("/logout")
	public String logout() {
		session.removeAttribute("loginUser");
		return "redirect:/main";
	}
	
	@GetMapping("/join")
	public String joinPage() {
		return "join"; // join.html
	}
	
	/**
	 * [수정됨]
	 * 회원가입 폼(/member/join) 처리를 위한 POST 메소드
	 */
	@PostMapping("/signup")
	public String signupProcess(@ModelAttribute Member member) {
		try {
			// 1. 가입 경로(web) 설정
			if (member.getKakaoId() != null) {
				member.setWeb("kakao");
			} else {
				member.setWeb("email");
			}
			
			// 2. (필수) 비밀번호 암호화
			member.setUserPw(passwordEncoder.encode(member.getUserPw()));
			
			// 3. DB에 저장
			memberRepository.save(member);
			
			System.out.println("회원가입 성공: " + member.getLoginId());

		} catch (Exception e) {
			// (중요) @Column(unique = true) 필드 (loginId, kakaoId, email) 중복 시 예외
			System.out.println("회원가입 실패 (중복 가능성): " + e.getMessage());
			return "redirect:/member/join?error=true";
		}
		
		// 4. 회원가입 성공 시 로그인 페이지로 리다이렉트 (가입 완료 메시지 포함)
		return "redirect:/member/login?signup=success";
	}
}