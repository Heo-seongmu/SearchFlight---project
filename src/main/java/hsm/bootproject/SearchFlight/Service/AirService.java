package hsm.bootproject.SearchFlight.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import hsm.bootproject.SearchFlight.domain.SearchLog;
import hsm.bootproject.SearchFlight.domain.basicArea;
import hsm.bootproject.SearchFlight.dto.ReturnFlightDto;
import hsm.bootproject.SearchFlight.dto.airParmDto;
import hsm.bootproject.SearchFlight.dto.airportDto;
import hsm.bootproject.SearchFlight.dto.searchAirDto;
import hsm.bootproject.SearchFlight.repository.SearchLogRepository;
import hsm.bootproject.SearchFlight.repository.basicAreaRepository;

@Service
public class AirService {

	@Autowired
	private basicAreaRepository basicareaRepository;

	@Autowired
	private SearchLogRepository searchLogRepository;
	
	private static final List<String> DOMESTIC_AIRPORTS = Arrays.asList(
		    "ICN", "GMP", "CJU", "PUS", "KWJ", "CJJ", "TAE", "RSU", "MWX", "YNY", "KPO", "USN", "HIN"
		);

	public String Translation(String text) {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

			// 1. API 주소를 파파고의 것으로 수정
			ClassicHttpRequest httpPost = ClassicRequestBuilder
					.post("https://papago.apigw.ntruss.com/nmt/v1/translation")
					// 2. 파라미터 괄호 오류 수정 및 올바른 파라미터 설정
					.setEntity(new UrlEncodedFormEntity(
							Arrays.asList(new BasicNameValuePair("source", "ko"),
									new BasicNameValuePair("target", "en"), new BasicNameValuePair("text", text)),
							StandardCharsets.UTF_8)) // "UTF-8" -> StandardCharsets.UTF_8
					.build();

			// 3. 올바른 헤더 추가 방식으로 수정
			httpPost.addHeader("x-ncp-apigw-api-key-id", "b3ledegk8h"); // 실제 Client ID로 교체하세요
			httpPost.addHeader("x-ncp-apigw-api-key", "VdEN5oguqSgTezFnDBMaP1pbYK2YEjCPsvflM8KC"); // 실제 Client Secret으로 교체하세요

			String data = httpclient.execute(httpPost, response -> {

				final HttpEntity entity = response.getEntity();
				String resData = EntityUtils.toString(entity);
				// 응답 본문을 닫아 리소스를 해제합니다.
				EntityUtils.consume(entity);
				return resData;
			});

			JsonObject message = JsonParser.parseString(data).getAsJsonObject().get("message").getAsJsonObject(); // Element 변환

			JsonObject result = message.get("result").getAsJsonObject();

			String trans = result.get("translatedText").getAsString();

			return trans;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String TranslationToEn(String text) {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

			// 1. API 주소를 파파고의 것으로 수정
			ClassicHttpRequest httpPost = ClassicRequestBuilder
					.post("https://papago.apigw.ntruss.com/nmt/v1/translation")
					// 2. 파라미터 괄호 오류 수정 및 올바른 파라미터 설정
					.setEntity(new UrlEncodedFormEntity(
							Arrays.asList(new BasicNameValuePair("source", "en"),
									new BasicNameValuePair("target", "ko"), new BasicNameValuePair("text", text)),
							StandardCharsets.UTF_8)) // "UTF-8" -> StandardCharsets.UTF_8
					.build();

			// 3. 올바른 헤더 추가 방식으로 수정
			httpPost.addHeader("x-ncp-apigw-api-key-id", "b3ledegk8h"); // 실제 Client ID로 교체하세요
			httpPost.addHeader("x-ncp-apigw-api-key", "VdEN5oguqSgTezFnDBMaP1pbYK2YEjCPsvflM8KC"); // 실제 Client Secret으로 교체하세요

			String data = httpclient.execute(httpPost, response -> {

				final HttpEntity entity = response.getEntity();
				String resData = EntityUtils.toString(entity);
				// 응답 본문을 닫아 리소스를 해제합니다.
				EntityUtils.consume(entity);
				return resData;
			});

			JsonObject message = JsonParser.parseString(data).getAsJsonObject().get("message").getAsJsonObject(); // Element 변환

			JsonObject result = message.get("result").getAsJsonObject();

			String trans = result.get("translatedText").getAsString();

			return trans;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String token() throws IOException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

			ClassicHttpRequest httpPost = ClassicRequestBuilder
					.post("https://test.api.amadeus.com/v1/security/oauth2/token")
					.setEntity(new UrlEncodedFormEntity(
							Arrays.asList(new BasicNameValuePair("grant_type", "client_credentials"),
									new BasicNameValuePair("client_id", "yTD8zuGsfrzLTuR3i7WO89rNKMyb1xQP"),
									new BasicNameValuePair("client_secret", "lokWISGS8IVtXJai"))))
					.build();

			// header
			httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

			// API 요청 후 응답 데이터
			String responseData = httpclient.execute(httpPost, response -> {

				final HttpEntity entity = response.getEntity();
				String resData = EntityUtils.toString(entity);
				EntityUtils.consume(entity);
				return resData;
			});
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(responseData);
			String accessToken = rootNode.get("access_token").asText();

			return accessToken; // access_token만 반환
		}
	}

	public String AirOfCity(String text) throws IOException {
		String auth = token();
		String translation = Translation(text);
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			ClassicHttpRequest httpGet = ClassicRequestBuilder
					.get("https://test.api.amadeus.com/v1/reference-data/locations/cities")
					// .addParameter("subType", "CITY")
					.addParameter("keyword", translation).build();
			httpGet.setHeader("Authorization", "Bearer " + auth);
			String data = httpclient.execute(httpGet, response -> {

				final HttpEntity entity1 = response.getEntity();
				String resData = EntityUtils.toString(entity1);
				EntityUtils.consume(entity1);
				return resData;
			});
			System.out.println(data);
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<airportDto> SearchAirports(String text) throws IOException {
		// 중복 제거를 위한 Map (Key: IATA코드, Value: AirportDto)
		Map<String, airportDto> resultsMap = new LinkedHashMap<>();

		List<basicArea> dbResults = basicareaRepository.findByKolocationContainingOrCountryContaining(text, text);
		for (basicArea area : dbResults) {
			airportDto dto = new airportDto(area.getKolocation(), area.getEnlocation(), area.getIataCode(), "DB");
			resultsMap.put(area.getIataCode(), dto);
		}

		String apiJsonString = AirOfCity(text);

		// API 응답이 비어있거나 유효하지 않을 경우를 대비한 방어 코드
		if (apiJsonString == null || apiJsonString.isEmpty()) {
			return new ArrayList<>(resultsMap.values()); // DB 결과만이라도 반환
		}

		ObjectMapper mapper = new ObjectMapper();
		JsonNode apiSearch = mapper.readTree(apiJsonString);

		// "data" 배열 파싱
		if (apiSearch.has("data")) {
			int apiResultsCount = 0; // API에서 추가된 결과 수를 세는 카운터
			final int MAX_API_RESULTS = 15; // 최대 결과 수를 상수로 정의하면 관리하기 편합니다.

			for (JsonNode location : apiSearch.get("data")) {
				// 이미 15개를 채웠으면 루프를 즉시 중단합니다.
				if (apiResultsCount >= MAX_API_RESULTS) {
					break;
				}

				String iataCode = location.path("iataCode").asText();

				// iataCode가 없거나, 이미 DB 결과에 포함되어 있다면 건너뛰기
				if (iataCode.isEmpty() || resultsMap.containsKey(iataCode)) {
					continue;
				}
				String enlocation = location.path("name").asText();
				String kolocation = TranslationToEn(enlocation);

				airportDto dto = new airportDto(kolocation, enlocation, iataCode, "API");

				// containsKey 체크를 위에서 했으므로 바로 put
				resultsMap.put(iataCode, dto);
				apiResultsCount++;
			}
		}

		// "included.airports" 객체 파싱 (세부 공항 정보)
		if (apiSearch.has("included") && apiSearch.get("included").has("airports")) {
			// 여기도 마찬가지로 path()를 사용해서 안전하게 파싱해야 합니다.
		}

		return new ArrayList<>(resultsMap.values());
	}
	
	@Cacheable(value = "flightSearchResults")
	public List<searchAirDto> searchAirPort(airParmDto airparmDto) throws IOException {
		String auth = token();

		try {
	        if (airparmDto.getArrivalCode() != null && !airparmDto.getArrivalCode().isEmpty()) {
	            SearchLog log = new SearchLog();
	            
	            String iataCode = airparmDto.getArrivalCode(); // 예: CJU, NRT
	            
	            log.setIataCode(iataCode);
	            log.setCityName(airparmDto.getArrivalKoLocation()); // 예: 제주, 도쿄
	            
	            // [핵심 로직] 도착 코드가 국내 공항 리스트에 있는지 확인
	            if (DOMESTIC_AIRPORTS.contains(iataCode)) {
	                log.setCountry("국내");
	            } else {
	                log.setCountry("해외");
	            }

	            searchLogRepository.save(log);
	            System.out.println(">> [DB 저장] " + log.getCityName() + " (" + log.getCountry() + ") 기록 완료");
	        }
	    } catch (Exception e) {
	        System.out.println(">> [DB 에러] 로그 저장 실패: " + e.getMessage());
	    }

		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			String nonstop = "false";
			if (airparmDto.isDirectFlight()) {
				nonstop = "true";
			}

			ClassicRequestBuilder requestBuilder = ClassicRequestBuilder
					.get("https://test.api.amadeus.com/v2/shopping/flight-offers")
					.addParameter("originLocationCode", airparmDto.getDepartureCode())
					.addParameter("destinationLocationCode", airparmDto.getArrivalCode())
					.addParameter("departureDate", airparmDto.getDepartureDate())
					.addParameter("adults", airparmDto.getAdults()).addParameter("children", airparmDto.getChildren())
					.addParameter("infants", airparmDto.getInfants())
					.addParameter("travelClass", airparmDto.getTravelClass()).addParameter("nonStop", nonstop);

			String returnDate = airparmDto.getReturnDate();
			if (returnDate != null && !returnDate.trim().isEmpty()) {
				requestBuilder.addParameter("returnDate", returnDate);
			}

			ClassicHttpRequest httpGet = requestBuilder.build();
			httpGet.setHeader("Authorization", "Bearer " + auth);

			String data = httpclient.execute(httpGet, response -> {
				final HttpEntity entity1 = response.getEntity();
				String resData = EntityUtils.toString(entity1);
				EntityUtils.consume(entity1);
				return resData;
			});
			System.out.println(data);
			List<searchAirDto> flightOfferList = new ArrayList<>();
			JsonObject rootObject = JsonParser.parseString(data).getAsJsonObject();

			if (rootObject.has("data")) {
				JsonArray flightOffersArray = rootObject.getAsJsonArray("data");

				int offerCount = 0;
				final int MAX_OFFERS = 15;

				for (JsonElement offerElement : flightOffersArray) {
					if (offerCount >= MAX_OFFERS) {
						break;
					}

					JsonObject flightOffer = offerElement.getAsJsonObject();
					searchAirDto dto = new searchAirDto();

					// ▼▼▼ [수정된 부분] API 응답에서 고유 id 값을 가져와 DTO에 설정합니다. ▼▼▼
					dto.setId(flightOffer.get("id").getAsString());
					// ▲▲▲ [수정된 부분] ▲▲▲

					JsonArray itineraries = flightOffer.getAsJsonArray("itineraries");
					int itineraryCount = itineraries.size();
					dto.setTripType(itineraryCount == 1 ? "one-way" : "round-trip");

					// ## 가는 편 정보 처리 ##
					JsonObject departureItinerary = itineraries.get(0).getAsJsonObject();

					// [추가] 1. 총 소요 시간 파싱 (PT20H10M -> 20시간 10분)
					if (departureItinerary.has("duration")) {
						String rawDuration = departureItinerary.get("duration").getAsString();
						dto.setTotalDuration(formatDuration(rawDuration));
					}

					JsonArray departureSegments = departureItinerary.getAsJsonArray("segments");
					dto.setDirectFlight(departureSegments.size() == 1);

					// [추가] 2. 경유지 추출 로직
					if (departureSegments.size() > 1) {
						List<String> layovers = new ArrayList<>();
						// 마지막 도착지를 제외한 모든 도착지가 경유지입니다.
						for (int i = 0; i < departureSegments.size() - 1; i++) {
							JsonObject segment = departureSegments.get(i).getAsJsonObject();
							String stopCode = segment.getAsJsonObject("arrival").get("iataCode").getAsString();
							layovers.add(stopCode);
						}
						dto.setLayoverNames(String.join(", ", layovers));
					} else {
						dto.setLayoverNames("");
					}

					JsonObject firstDepartureSegment = departureSegments.get(0).getAsJsonObject();
					JsonObject lastDepartureSegment = departureSegments.get(departureSegments.size() - 1)
							.getAsJsonObject();

					String departureAt = firstDepartureSegment.getAsJsonObject("departure").get("at").getAsString();
					dto.setDepartureDate(departureAt.substring(0, 10));
					dto.setDepartureTime(departureAt.substring(11, 16));
					dto.setDepartureCode(
							firstDepartureSegment.getAsJsonObject("departure").get("iataCode").getAsString());
					dto.setCarrierCode(firstDepartureSegment.get("carrierCode").getAsString());

					String arrivalAt = lastDepartureSegment.getAsJsonObject("arrival").get("at").getAsString();
					dto.setArrivalTime(arrivalAt.substring(11, 16));
					dto.setArrivalCode(lastDepartureSegment.getAsJsonObject("arrival").get("iataCode").getAsString());
					dto.setArrivalDate(arrivalAt.substring(0, 10));
					
					try {
					    LocalDate depDate = LocalDate.parse(dto.getDepartureDate());
					    LocalDate arrDate = LocalDate.parse(dto.getArrivalDate());
					    // 날짜 차이 계산 (arr - dep)
					    long diff = ChronoUnit.DAYS.between(depDate, arrDate);
					    dto.setDayDifference((int) diff);
					} catch (Exception e) {
					    // 날짜 파싱 에러 시 기본값 0
					    dto.setDayDifference(0);
					}

					// ## 오는 편 정보 처리 ##
					if (itineraryCount > 1) {
						JsonObject returnItinerary = itineraries.get(1).getAsJsonObject();
						JsonArray returnSegments = returnItinerary.getAsJsonArray("segments");
						dto.setReturnDirectFlight(returnSegments.size() == 1);

						JsonObject firstReturnSegment = returnSegments.get(0).getAsJsonObject();
						JsonObject lastReturnSegment = returnSegments.get(returnSegments.size() - 1).getAsJsonObject();

						String returnDepartureAt = firstReturnSegment.getAsJsonObject("departure").get("at")
								.getAsString();
						dto.setReturnDepartureDate(returnDepartureAt.substring(0, 10));
						dto.setReturnDepartureTime(returnDepartureAt.substring(11, 16));
						dto.setReturnDepartureCode(
								firstReturnSegment.getAsJsonObject("departure").get("iataCode").getAsString());
						dto.setReturnCarrierCode(firstReturnSegment.get("carrierCode").getAsString());

						String returnArrivalAt = lastReturnSegment.getAsJsonObject("arrival").get("at").getAsString();
						dto.setReturnArrivalTime(returnArrivalAt.substring(11, 16));
						dto.setReturnArrivalCode(
								lastReturnSegment.getAsJsonObject("arrival").get("iataCode").getAsString());
						dto.setReturnArrivalDate(returnArrivalAt.substring(0, 10));
					}

					// ## 나머지 정보 처리 ##
					JsonArray travelerPricings = flightOffer.getAsJsonArray("travelerPricings");
					int adults = 0, children = 0, infants = 0;
					for (JsonElement traveler : travelerPricings) {
						String type = traveler.getAsJsonObject().get("travelerType").getAsString();
						if ("ADULT".equals(type))
							adults++;
						if ("CHILD".equals(type))
							children++;
						if ("INFANT".equals(type))
							infants++;
					}
					dto.setAdults(String.valueOf(adults));
					dto.setChildren(String.valueOf(children));
					dto.setInfants(String.valueOf(infants));

					String travelClass = travelerPricings.get(0).getAsJsonObject()
							.getAsJsonArray("fareDetailsBySegment").get(0).getAsJsonObject().get("cabin").getAsString();
					dto.setTravelClass(travelClass);

					dto.setNumberOfBookableSeats(flightOffer.get("numberOfBookableSeats").getAsString());

					String totalPrice = flightOffer.getAsJsonObject("price").get("total").getAsString();
					double priceAsDouble = Double.parseDouble(totalPrice);
					double priceInWon = priceAsDouble * 1650;
					long roundedPriceInWon = Math.round(priceInWon);
					String formattedPrice = String.format("%,d", roundedPriceInWon);
					dto.setTotalPrice(formattedPrice);
					dto.setRawTotalPrice(roundedPriceInWon); // 데이터 전송용 (쉼표 X)
					flightOfferList.add(dto);
					offerCount++;
				}
			} else {
				System.out.println("받아온 데이터가 없습니다");
			}
			System.out.println(">>> [AirService] searchAirPort 메소드가 반환하는 리스트 크기: " + flightOfferList.size());
			return flightOfferList;

		} catch (Exception e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}
	
	@Cacheable(value = "surroundingPrices")
	public Map<String, String> getSurroundingPrices(airParmDto originalDto) {
	    Map<String, String> priceMap = new ConcurrentHashMap<>(); 
	    
	    String depDateStr = originalDto.getDepartureDate();
	    if (depDateStr == null || depDateStr.trim().isEmpty()) {
	        return priceMap; 
	    }

	    // 🟢 [중요] token() 메서드도 예외를 던질 수 있다면 try-catch로 감싸거나, 
	    // getSurroundingPrices 메서드 선언부에서 throws를 빼고 내부 처리해야 합니다.
	    String auth = "";
	    try {
	        auth = token(); 
	    } catch (IOException e) {
	        e.printStackTrace();
	        return priceMap; // 토큰 발급 실패시 빈 맵 반환
	    }

	    LocalDate startDepDate;
	    try {
	        startDepDate = LocalDate.parse(depDateStr);
	    } catch (Exception e) {
	        return priceMap;
	    }

	    long duration = 0;
	    boolean isRoundTrip = "round-trip".equals(originalDto.getTripType());

	    if (isRoundTrip) {
	        String retDateStr = originalDto.getReturnDate();
	        if (retDateStr != null && !retDateStr.trim().isEmpty()) {
	            try {
	                LocalDate startRetDate = LocalDate.parse(retDateStr);
	                duration = ChronoUnit.DAYS.between(startDepDate, startRetDate);
	            } catch (Exception e) {
	                // ignore
	            }
	        }
	    }
	    
	    final long finalDuration = duration; 
	    final String authToken = auth; 

	    List<CompletableFuture<Void>> futures = IntStream.rangeClosed(-2, 2)
	        .mapToObj(i -> CompletableFuture.runAsync(() -> {
	            // 🟢 람다식 내부: 여기서 발생하는 모든 예외는 내부에서 처리해야 함
	            try {
	                LocalDate targetDepDate = startDepDate.plusDays(i);
	                
	                if (targetDepDate.isBefore(LocalDate.now())) {
	                    return; 
	                }

	                String targetDepDateStr = targetDepDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
	                String targetRetDateStr = null;

	                if (isRoundTrip) {
	                    LocalDate targetRetDate = targetDepDate.plusDays(finalDuration);
	                    targetRetDateStr = targetRetDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
	                }

	                // fetchCheapestPrice는 내부에서 예외를 먹고 null을 리턴하도록 만들었으므로 안전함
	                String price = fetchCheapestPrice(originalDto, targetDepDateStr, targetRetDateStr, authToken);
	                
	                if (price != null) {
	                    priceMap.put(targetDepDateStr, price);
	                } else {
	                    priceMap.put(targetDepDateStr, "-");
	                }

	            } catch (Exception e) {
	                e.printStackTrace(); // 람다 내부 에러 출력
	            }
	        }))
	        .toList();

	    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

	    return priceMap;
	}

    private String fetchCheapestPrice(airParmDto dto, String depDate, String retDate, String authToken) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            
            String nonstop = dto.isDirectFlight() ? "true" : "false";

            ClassicRequestBuilder requestBuilder = ClassicRequestBuilder
                    .get("https://test.api.amadeus.com/v2/shopping/flight-offers")
                    .addParameter("originLocationCode", dto.getDepartureCode())
                    .addParameter("destinationLocationCode", dto.getArrivalCode())
                    .addParameter("departureDate", depDate)
                    .addParameter("adults", dto.getAdults())
                    .addParameter("children", dto.getChildren())
                    .addParameter("infants", dto.getInfants())
                    .addParameter("travelClass", dto.getTravelClass())
                    .addParameter("nonStop", nonstop)
                    .addParameter("max", "1"); 

            if (retDate != null) {
                requestBuilder.addParameter("returnDate", retDate);
            }

            ClassicHttpRequest httpGet = requestBuilder.build();
            httpGet.setHeader("Authorization", "Bearer " + authToken);

            String responseData = httpclient.execute(httpGet, response -> {
                final HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity);
            });

            // 🟢 [디버깅 로그 추가] API 응답 확인
            System.out.println("=== [" + depDate + "] API 조회 결과 ===");
            // 내용이 너무 길면 앞부분만 출력
            if(responseData.length() > 200) {
                 System.out.println(responseData.substring(0, 200) + "..."); 
            } else {
                 System.out.println(responseData);
            }

            JsonObject root = JsonParser.parseString(responseData).getAsJsonObject();
            
            // 에러 응답인지 확인 (예: rate limit)
            if (root.has("errors")) {
                System.out.println("🚨 API 에러 발생: " + root.get("errors"));
                return null;
            }

            if (root.has("data")) {
                JsonArray dataArray = root.getAsJsonArray("data");
                if (dataArray.size() > 0) {
                    JsonObject firstOffer = dataArray.get(0).getAsJsonObject();
                    String totalStr = firstOffer.getAsJsonObject("price").get("total").getAsString();
                    
                    double price = Double.parseDouble(totalStr);
                    long wonPrice = Math.round(price * 1650);
                    
                    String result = String.format("%,d원", wonPrice);
                    System.out.println("✅ 가격 찾음: " + result);
                    return result;
                } else {
                    System.out.println("⚠️ 데이터 없음 (data array is empty)");
                }
            }
        } catch (Exception e) {
            System.out.println("❌ 예외 발생: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

	public List<ReturnFlightDto> findReturnFlights(airParmDto airparmDto, String selectedCarrierCode,
			String selectedDepartureTime) throws IOException {

		// 1. 기존 '가는 편' 검색 로직을 재실행하여 전체 왕복 항공권 목록을 다시 가져옵니다.
		List<searchAirDto> allRoundTripOffers = searchAirPort(airparmDto);

		List<ReturnFlightDto> matchedReturnFlights = new ArrayList<>();

		// 2. 전체 목록에서 사용자가 선택한 '가는 편'과 일치하는 항공권을 찾습니다.
		for (searchAirDto offer : allRoundTripOffers) {
			if (offer.getCarrierCode().equals(selectedCarrierCode)
					&& offer.getDepartureTime().equals(selectedDepartureTime)) {

				// 3. 일치하는 항공권의 '오는 편' 정보를 새로운 DTO에 담습니다.
				ReturnFlightDto returnDto = new ReturnFlightDto();
				returnDto.setId(offer.getId() + "_return"); // 고유 ID를 만들어 줌
				returnDto.setReturnCarrierCode(offer.getReturnCarrierCode());
				returnDto.setReturnDepartureTime(offer.getReturnDepartureTime());
				returnDto.setReturnArrivalTime(offer.getReturnArrivalTime());
				returnDto.setReturnDirectFlight(offer.isReturnDirectFlight());

				returnDto.setReturnDepartureDate(offer.getReturnDepartureDate());
				returnDto.setReturnArrivalDate(offer.getReturnArrivalDate());
				
				try {
				    LocalDate retDepDate = LocalDate.parse(offer.getReturnDepartureDate());
				    LocalDate retArrDate = LocalDate.parse(offer.getReturnArrivalDate());
				    long diff = ChronoUnit.DAYS.between(retDepDate, retArrDate);
				    returnDto.setReturnDayDifference((int) diff);
				} catch (Exception e) {
				    returnDto.setReturnDayDifference(0);
				}

				// 중요: searchAirDto의 rawTotalPrice는 왕복 총액입니다.
				// 여기서는 편의상 그대로 사용하지만, 편도 가격을 별도로 계산해야 할 수도 있습니다.
				returnDto.setReturnTotalPrice(offer.getRawTotalPrice());
				returnDto.setReturnDepartureCode(offer.getReturnDepartureCode());
				returnDto.setReturnArrivalCode(offer.getReturnArrivalCode());
				matchedReturnFlights.add(returnDto);
			}
		}

		// 4. 일치하는 '오는 편' 항공권 목록을 반환합니다.
		return matchedReturnFlights;
	}

	public boolean isDomesticAirport(String iataCode) {
		if (iataCode == null || iataCode.isEmpty()) {
			return false;
		}

		// 1. Repository를 통해 IATA 코드로 공항 정보 조회
		basicArea airport = basicareaRepository.findByIataCode(iataCode);

		if (airport != null) {
			// 2. 조회된 공항의 국가가 "대한민국"인지 확인
			return "대한민국".equals(airport.getCountry());
		}

		// 3. DB에 정보가 없는 경우, 일단 국제선으로 간주 (안전 조치)
		return false;
	}

	// [추가] ISO 8601 시간 포맷 변환 헬퍼 메소드 (PT20H10M -> 20시간 10분)
	private String formatDuration(String ptDuration) {
		if (ptDuration == null || ptDuration.isEmpty())
			return "";

		String time = ptDuration.replace("PT", ""); // "20H10M"
		String hours = "0";
		String minutes = "0";

		int hIndex = time.indexOf("H");
		int mIndex = time.indexOf("M");

		if (hIndex != -1) {
			hours = time.substring(0, hIndex);
		}

		if (mIndex != -1) {
			// H가 있으면 H 뒤부터 M 앞까지, 없으면 처음부터 M 앞까지
			int start = (hIndex != -1) ? hIndex + 1 : 0;
			minutes = time.substring(start, mIndex);
		}

		return hours + "시간 " + minutes + "분";
	}

}