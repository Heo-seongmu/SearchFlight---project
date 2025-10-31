package hsm.bootproject.SearchFlight.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hsm.bootproject.SearchFlight.Service.AirService;
import hsm.bootproject.SearchFlight.dto.airParmDto;
import hsm.bootproject.SearchFlight.dto.searchAirDto;

@RestController
public class returnController {

	@Autowired
	private AirService airService;
	
	 @GetMapping("/api/return-flights") // 이 URL로 AJAX 요청을 보낼 겁니다.
	 public List<searchAirDto> getReturnFlights(
	            // AJAX 요청 시 처음 검색했던 조건들을 다시 보내줘야 합니다.
	            airParmDto airparmDto, 
	            // 그리고 사용자가 선택한 '가는 편'의 식별 정보도 함께 받습니다.
	            @RequestParam("selectedDepartureTime") String selectedDepartureTime,
	            @RequestParam("selectedCarrierCode") String selectedCarrierCode
	    ) throws IOException {

	        // 1. [재사용] 기존 서비스를 호출해 모든 왕복 조합을 다시 가져옵니다.
		 	System.out.println(selectedDepartureTime + "-" + selectedCarrierCode);
	        List<searchAirDto> allOffers = airService.searchAirPort(airparmDto);

	        // 2. [필터링] Java Stream을 사용해, 사용자가 선택한 '가는 편'과
	        //    정보가 일치하는 조합들만 남깁니다.
	        List<searchAirDto> filteredOffers = allOffers.stream()
	            .filter(offer -> 
	                offer.getDepartureTime().equals(selectedDepartureTime) &&
	                offer.getCarrierCode().equals(selectedCarrierCode)
	            )
	            .collect(Collectors.toList());

	        // 3. 필터링된 결과를 반환합니다. 이 목록에는 '오는 편' 정보가 다양하게 들어있습니다.
	        return filteredOffers;
	    }
	
}
