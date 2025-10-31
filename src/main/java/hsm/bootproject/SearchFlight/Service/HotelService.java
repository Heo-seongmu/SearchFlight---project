package hsm.bootproject.SearchFlight.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import hsm.bootproject.SearchFlight.dto.HotelInfo;
import hsm.bootproject.SearchFlight.dto.HotelResponse;
// (평점 관련 DTO 임포트 삭제)

@Service
public class HotelService {
	
	private final ObjectMapper objectMapper;

    @Autowired
    public HotelService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

	public String token() throws IOException{
		 // (token 메서드 동일 - 수정 없음)
		 try ( CloseableHttpClient httpclient = HttpClients.createDefault() ) {

	          ClassicHttpRequest httpPost = ClassicRequestBuilder.post("https://test.api.amadeus.com/v1/security/oauth2/token")
	                                        .setEntity(
	                                        new UrlEncodedFormEntity(Arrays.asList(
	                                        		new BasicNameValuePair("grant_type", "client_credentials"),
	                                        		new BasicNameValuePair("client_id", "yTD8zuGsfrzLTuR3i7WO89rNKMyb1xQP"),
	                                        		new BasicNameValuePair("client_secret", "lokWISGS8IVtXJai")
	                                        		)
	                                        	 )			
	                                        	)
	                                        .build();
			    
	          httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
			    
	      String responseData = httpclient.execute(httpPost, response -> {
	          
		    final HttpEntity entity = response.getEntity();
	          String resData = EntityUtils.toString(entity);
	          EntityUtils.consume(entity);
	          return resData;
	       });
	      ObjectMapper objectMapper = new ObjectMapper();
	      JsonNode rootNode = objectMapper.readTree(responseData);
	      String accessToken = rootNode.get("access_token").asText();

	      return accessToken;
	  }
	}
	
    /**
     * [수정] 평점 로직이 모두 제거되고, 번역 기능만 남았습니다.
     */
    public List<HotelInfo> hotelList(String destination) throws IOException {
        
        // 1. Amadeus 'by-city' API 호출 (호텔 10개 목록 가져오기)
        List<HotelInfo> hotels = getHotelsByCity(destination);
        
        if (hotels == null || hotels.isEmpty()) {
        	System.out.println("===== [HotelService] 'by-city' API 결과: 호텔 없음. 로직 종료.");
            return Collections.emptyList();
        }
        System.out.println("===== [HotelService] 'by-city' API 결과: " + hotels.size() + "개 호텔 찾음.");

        
        // 2. [ 평점 관련 로직 모두 삭제 ]
        // (배치 처리, getHotelSentiments 호출, Map 생성 등 모두 삭제)


        // 3. [ 최종 ] 호텔 목록(hotels)을 순회하며 번역
        System.out.println("===== [HotelService] 호텔 이름 번역 시작 =====");
        for (HotelInfo hotel : hotels) {
            // (기존) Papago 번역
            String englishName = hotel.getName();
            String koreanName = translation(englishName);
            hotel.setKoreanName(koreanName);
            
            // (평점 병합 로직 삭제)
        }
        System.out.println("===== [HotelService] 번역 완료 =====");
        
        return hotels; // 번역과 평점이 모두 채워진 리스트 반환
    }

    private List<HotelInfo> getHotelsByCity(String destination) throws IOException {
        String auth = token(); 
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            ClassicRequestBuilder requestBuilder = ClassicRequestBuilder.get("https://test.api.amadeus.com/v1/reference-data/locations/hotels/by-city")
                    .addParameter("cityCode", destination)
                    .addParameter("radius", "15");
            
            ClassicHttpRequest httpGet = requestBuilder.build();
            httpGet.setHeader("Authorization", "Bearer " + auth);
            
            String data = httpclient.execute(httpGet, response -> {
                final HttpEntity entity1 = response.getEntity();
                String resData = EntityUtils.toString(entity1);
                EntityUtils.consume(entity1);
                return resData;
            });
            
            HotelResponse hotelResponse = objectMapper.readValue(data, HotelResponse.class);
            
            if (hotelResponse != null && hotelResponse.getData() != null) {
                
                return hotelResponse.getData().stream()
                        .limit(10)
                        .collect(Collectors.toList());
                        
            } else {
                return Collections.emptyList();
            }
        }
    }
    
    /**
     * [삭제] getHotelSentiments 메서드 전체가 삭제되었습니다.
     */
    // private List<HotelSentimentData> getHotelSentiments(String hotelIds) throws IOException { ... }

    /**
     * (translation 메서드는 수정 없음)
     */
    public String translation(String text) {
    	 try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
           ClassicHttpRequest httpPost = ClassicRequestBuilder.post("https://papago.apigw.ntruss.com/nmt/v1/translation")
           		   .setEntity(new UrlEncodedFormEntity(Arrays.asList(
                              new BasicNameValuePair("source", "en"),
                              new BasicNameValuePair("target", "ko"),
                              new BasicNameValuePair("text", text)
                      ), StandardCharsets.UTF_8))
                      .build();

           httpPost.addHeader("x-ncp-apigw-api-key-id", "b3ledegk8h"); 
           httpPost.addHeader("x-ncp-apigw-api-key", "VdEN5oguqSgTezFnDBMaP1pbYK2YEjCPsvflM8KC");   

           String data = httpclient.execute(httpPost, response -> {
              
               final HttpEntity entity = response.getEntity();
               String resData = EntityUtils.toString(entity);
               EntityUtils.consume(entity);
               return resData;
           });
           
           JsonObject message =  JsonParser.parseString(data).getAsJsonObject().get("message").getAsJsonObject(); 
			
			JsonObject result = message.get("result").getAsJsonObject();
			
			String trans = result.get("translatedText").getAsString();
        
           return trans;
       } catch (Exception e) {
           e.printStackTrace();
           return text;
       }
   }
}