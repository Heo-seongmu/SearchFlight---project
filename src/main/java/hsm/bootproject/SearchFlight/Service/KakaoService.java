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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import hsm.bootproject.SearchFlight.domain.Member;
import hsm.bootproject.SearchFlight.repository.UserRepository;

@Service
public class KakaoService {
	
	@Value("${kakao.api.key}")
	private String client_id;
	
	@Value("${kakao.redirect.uri}")
	private String redirect_uri;
	
	@Autowired
	private UserRepository userRepository;

	public String kakao_request_token(String code) {
		System.out.println("2");
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
		System.out.println("3");
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

	public Member getKakaoUserInfo(String code) {
		System.out.println("1");
		    // 1. 카카오 서버로부터 토큰을 요청합니다.
		    String tokenResponse = kakao_request_token(code);
		    
		    // ★ 디버깅 핵심: 카카오가 보낸 원본 응답을 반드시 출력해서 확인합니다.
		    System.out.println("카카오 토큰 응답: " + tokenResponse); 
		    
		    // 응답이 null이거나 비어있으면 더 이상 진행하지 않습니다.
		    if (tokenResponse == null || tokenResponse.isEmpty()) {
		        System.out.println("카카오로부터 토큰 응답을 받지 못했습니다.");
		        return null;
		    }

		    JsonObject tokenObj = JsonParser.parseString(tokenResponse).getAsJsonObject();

		    // ★ 안정성 강화 1: "access_token" 키가 있는지 먼저 확인합니다.
		    if (!tokenObj.has("access_token")) {
		        System.out.println("응답에 액세스 토큰이 없습니다. 에러 응답일 수 있습니다.");
		        return null; 
		    }

		    String access_token = tokenObj.get("access_token").getAsString();

		    // 2. 발급받은 토큰으로 사용자 정보를 요청합니다.
		    String userInfoResponse = kakao_request_userInfo(access_token);
		    
		    // ★ 디버깅 핵심: 사용자 정보 응답 원본을 출력해서 확인합니다.
		    System.out.println("카카오 사용자 정보 응답: " + userInfoResponse);

		    if (userInfoResponse == null || userInfoResponse.isEmpty()) {
		        System.out.println("카카오로부터 사용자 정보 응답을 받지 못했습니다.");
		        return null;
		    }

		    JsonObject userInfoObj = JsonParser.parseString(userInfoResponse).getAsJsonObject();
		    
		    // 3. 필요한 사용자 정보를 안전하게 추출합니다.
		    String UserId = userInfoObj.get("id").getAsString();
		    String UserName = "[닉네임 없음]"; // 기본값 설정

		    // ★ 안정성 강화 2: "properties" 객체가 있는지, 그 안에 "nickname"이 있는지 단계별로 확인합니다.
		    if (userInfoObj.has("properties")) {
		        JsonObject properties = userInfoObj.getAsJsonObject("properties");
		        if (properties.has("nickname")) {
		        	UserName = properties.get("nickname").getAsString();
		        }
		    }
		    Member member = userRepository.findByUserIdAndUserName(UserId,UserName);
		    if(member != null) {
		    	member.setWeb("카카오톡");
			    return member;
		    }else {
		    	member = new Member();
		    	member.setUserId(UserId);
		    	member.setUserName(UserName);
		    	member.setWeb("카카오톡");
		    	userRepository.save(member);
		    	return member;
		    }
		   		    
		}

}
