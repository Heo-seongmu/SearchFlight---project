package hsm.bootproject.SearchFlight.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import hsm.bootproject.SearchFlight.domain.basicArea;
import hsm.bootproject.SearchFlight.dto.ReturnFlightDto;
import hsm.bootproject.SearchFlight.dto.airParmDto;
import hsm.bootproject.SearchFlight.dto.airportDto;
import hsm.bootproject.SearchFlight.dto.searchAirDto;
import hsm.bootproject.SearchFlight.repository.basicAreaRepository;

@Service
public class AirService {

	@Autowired
	private basicAreaRepository basicareaRepository;

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
			httpPost.addHeader("x-ncp-apigw-api-key", "VdEN5oguqSgTezFnDBMaP1pbYK2YEjCPsvflM8KC"); // 실제 Client Secret으로
																									// 교체하세요

			String data = httpclient.execute(httpPost, response -> {

				final HttpEntity entity = response.getEntity();
				String resData = EntityUtils.toString(entity);
				// 응답 본문을 닫아 리소스를 해제합니다.
				EntityUtils.consume(entity);
				return resData;
			});

			JsonObject message = JsonParser.parseString(data).getAsJsonObject().get("message").getAsJsonObject(); // Element
																													// 변환

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
			httpPost.addHeader("x-ncp-apigw-api-key", "VdEN5oguqSgTezFnDBMaP1pbYK2YEjCPsvflM8KC"); // 실제 Client Secret으로
																									// 교체하세요

			String data = httpclient.execute(httpPost, response -> {

				final HttpEntity entity = response.getEntity();
				String resData = EntityUtils.toString(entity);
				// 응답 본문을 닫아 리소스를 해제합니다.
				EntityUtils.consume(entity);
				return resData;
			});

			JsonObject message = JsonParser.parseString(data).getAsJsonObject().get("message").getAsJsonObject(); // Element
																													// 변환

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

	public List<searchAirDto> searchAirPort(airParmDto airparmDto) throws IOException {
		String auth = token();

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

			List<searchAirDto> flightOfferList = new ArrayList<>();
			JsonObject rootObject = JsonParser.parseString(data).getAsJsonObject();

			if (rootObject.has("data")) {
				JsonArray flightOffersArray = rootObject.getAsJsonArray("data");

				int offerCount = 0;
				final int MAX_OFFERS = 10;

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
					JsonArray departureSegments = departureItinerary.getAsJsonArray("segments");
					dto.setDirectFlight(departureSegments.size() == 1);

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

}
