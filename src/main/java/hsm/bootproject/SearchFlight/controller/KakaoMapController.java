package hsm.bootproject.SearchFlight.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hsm.bootproject.SearchFlight.Service.KakaoMapService;
import hsm.bootproject.SearchFlight.dto.KakaoRouteDto;

@RestController
public class KakaoMapController {

	@Autowired
    private KakaoMapService kakaoMapService;

    @GetMapping("/api/kakao-directions")
    public ResponseEntity<KakaoRouteDto> getDirections(
            @RequestParam("originLat") String originLat,
            @RequestParam("originLng") String originLng,
            @RequestParam("destLat") String destLat,
            @RequestParam("destLng") String destLng) {
        
        try {
            KakaoRouteDto route = kakaoMapService.getDirections(originLat, originLng, destLat, destLng);
            if (route != null) {
                return ResponseEntity.ok(route); // 200 OK + 경로 정보
            } else {
                // 카카오 API가 경로를 못찾은 경우 (ZERO_RESULTS 등)
                return ResponseEntity.noContent().build(); // 204 No Content
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build(); // 500 Server Error
        }
    }
	
}
