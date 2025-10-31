package hsm.bootproject.SearchFlight.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import hsm.bootproject.SearchFlight.dto.KakaoRouteDto; // (아래 2번에서 생성할 DTO)

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.ArrayList;

@Service
public class KakaoMapService {

    // 1. application.properties에서 키를 읽어옵니다.
    @Value("${kakao.api.key}")
    private String kakaoRestApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 카카오 API를 호출하여 경로 정보를 가져옵니다.
     */
    public KakaoRouteDto getDirections(String originLat, String originLng, String destLat, String destLng) throws IOException {
        
        String url = "https://apis-navi.kakaomobility.com/v1/directions";
        
        // 카카오 API는 파라미터 순서가 "lng,lat" 입니다.
        URI uri = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("origin", originLng + "," + originLat)
                .queryParam("destination", destLng + "," + destLat)
                .build(true).toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "KakaoAK " + kakaoRestApiKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            String response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class).getBody();
            
            System.out.println("===== [KakaoMapService Debug] Kakao API Response: " + response);

            JsonNode root = objectMapper.readTree(response);
            
            // 경로가 없는 경우 (trans_id가 없음)
            if (!root.has("routes") || root.get("routes").isEmpty()) {
                System.out.println("===== [KakaoMapService Debug] Kakao API가 경로를 반환하지 않음 (ZERO_RESULTS)");
                return null;
            }
            
            // 응답이 너무 복잡하므로, 필요한 데이터만 추출합니다.
            JsonNode summary = root.get("routes").get(0).get("summary");
            JsonNode sections = root.get("routes").get(0).get("sections").get(0);
            
            KakaoRouteDto route = new KakaoRouteDto();
            route.setDuration(summary.get("duration").asInt()); // 총 소요 시간 (초)
            route.setDistance(summary.get("distance").asInt()); // 총 거리 (미터)
            
            // 지도에 그릴 Polyline 좌표 추출
            // (카카오는 도로별로 좌표(vertexes)를 [lng1, lat1, lng2, lat2...] 형태로 제공)
            List<List<Double>> path = new ArrayList<>();
            for (JsonNode road : sections.get("roads")) {
                JsonNode verticesNode = road.get("vertexes");
                List<Double> vertices = new ArrayList<>();
                for (JsonNode vertex : verticesNode) {
                    vertices.add(vertex.asDouble());
                }
                
                // [lng, lat] 쌍으로 변환하여 path에 추가
                for (int i = 0; i < vertices.size(); i += 2) {
                    path.add(List.of(vertices.get(i), vertices.get(i+1)));
                }
            }
            
            route.setPath(path);
            return route;
            
        } catch (Exception e) {
            System.err.println("===== [KakaoMapService Error] " + e.getMessage());
            return null; // 오류 발생 시 null 반환
        }
    }
}
