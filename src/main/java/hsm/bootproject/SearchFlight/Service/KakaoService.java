package hsm.bootproject.SearchFlight.Service;

import java.util.Arrays;

import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service
public class KakaoService {
	
	@Value("${kakao.api.key}")
	private String client_id;
	
	@Value("${kakao.redirect.uri}")
	private String redirect_uri;
	
	public String kakao_request_token(String code) {
		System.out.println("2. 토큰 요청");
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {		  
			   
			ClassicHttpRequest httpPost = ClassicRequestBuilder.post("https://kauth.kakao.com/oauth/token")
		            .setEntity(new UrlEncodedFormEntity(Arrays.asList(
		                    new BasicNameValuePair("grant_type", "authorization_code"),
		                    new BasicNameValuePair("client_id", client_id),
		                    new BasicNameValuePair("redirect_uri",redirect_uri),
		                    new BasicNameValuePair("code",code))))
		            .build();
			httpPost.addHeader("Content-Type","application/x-www-form-urlencoded;charset=utf-8");
			
		   String data = httpclient.execute(httpPost, response -> {
		        System.out.println(response.getCode() + " " + response.getReasonPhrase());
		        final HttpEntity entity2 = (HttpEntity) response.getEntity();		        
		        String resData = EntityUtils.toString(entity2);
		        return resData;
		    });
		   return data;
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}		
	}
	
	private String kakao_request_userInfo(String access_token) {
		System.out.println("3. 사용자 정보 요청");
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {		  
			   
			ClassicHttpRequest httpPost = ClassicRequestBuilder.post("https://kapi.kakao.com/v2/user/me")
		            
		            .build();
			httpPost.addHeader("Authorization","Bearer " +access_token);			
			httpPost.addHeader("Content-Type","application/x-www-form-urlencoded;charset=utf-8");
			
		   String data = httpclient.execute(httpPost, response -> {
		        System.out.println(response.getCode() + " " + response.getReasonPhrase());
		        final HttpEntity entity2 = response.getEntity();		        
		        String resData = EntityUtils.toString(entity2);
		        return resData;
		    });
		   return data;
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * [수정됨]
	 * DB 로직을 제거하고, 카카오에서 받은 프로필 정보(JsonObject)를 반환합니다.
	 */
	public JsonObject getKakaoUserProfile(String code) {
		System.out.println("1. 카카오 프로필 정보 요청 시작");
		
		String tokenResponse = kakao_request_token(code);
		System.out.println("카카오 토큰 응답: " + tokenResponse);
		
		if (tokenResponse == null || tokenResponse.isEmpty()) {
			System.out.println("카카오로부터 토큰 응답을 받지 못했습니다.");
			return null;
		}
		
		JsonObject tokenObj = JsonParser.parseString(tokenResponse).getAsJsonObject();
		if (!tokenObj.has("access_token")) {
			System.out.println("응답에 액세스 토큰이 없습니다.");
			return null;
		}
		
		String access_token = tokenObj.get("access_token").getAsString();

		// 2. 발급받은 토큰으로 사용자 정보를 요청합니다.
		String userInfoResponse = kakao_request_userInfo(access_token);
		System.out.println("카카오 사용자 정보 응답: " + userInfoResponse);
		
		if (userInfoResponse == null || userInfoResponse.isEmpty()) {
			System.out.println("카카오로부터 사용자 정보 응답을 받지 못했습니다.");
			return null;
		}

		// 3. JsonObject 자체를 반환합니다. (파싱 및 DB 로직은 컨트롤러에서)
		return JsonParser.parseString(userInfoResponse).getAsJsonObject();
	}
}