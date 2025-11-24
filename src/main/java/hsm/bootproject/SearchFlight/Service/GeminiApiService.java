package hsm.bootproject.SearchFlight.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GeminiApiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    // gemini-2.5-flash 모델 사용 (기존 유지)
    private final String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    public GeminiApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * [모드 1] 여행지 추천 API 호출 (수정됨: 3곳 추천 배열 반환)
     */
    public String callRecommendationApi(List<Map<String, String>> conversationHistory) {
        
        String systemPromptText = """
        당신은 '무성의 여행'이라는 이름의 전문 여행 추천 챗봇입니다.
        모든 답변은 성의있게 답변하고 한국어로 대답합니다. 여행 전문가의 말투로 친절한 존댓말을 사용하세요.
        
        사용자는 [선택 조건]과 [사용자 추가 요청] 형식으로 정보를 줄 것입니다.
        이 조건('출발지', '출발 날짜', '지역', '테마', '기간', '경비')을 모두 고려하여 **가장 적합한 여행지 3곳**을 추천해주세요.
        
        **[가장 중요]**
        1. '출발지'를 보고 실제 항공권 조회가 가능한 곳이어야 합니다.
        2. '출발 날짜'를 보고 해당 여행지의 **계절적 특징(날씨, 축제, 성수기 등)**을 반드시 고려하세요.
        3. 3곳의 여행지는 서로 다른 매력을 가진 곳으로 선정하는 것이 좋습니다.
        
        **[필수 응답 형식]**
        당신의 답변은 **반드시** 아래와 같은 JSON 형식이어야 합니다. 
        마크다운(```json)이나 추가적인 설명 텍스트를 붙이지 말고 오직 JSON 객체만 반환하세요.
        
        {
          "chat_response": "사용자에게 건네는 전체적인 인사말 및 추천 요약 (예: '고객님, 요청하신 12월 겨울 힐링 테마에 딱 맞는 여행지 3곳을 찾아왔습니다.')",
          "recommendations": [
            {
              "city": "첫번째 도시명 (예: 일본 삿포로)",
              "country": "국가명 (예: 일본)",
              "iataCode": "3자리 IATA 공항 코드 (예: CTS)",
              "reason": "추천 이유 (계절, 예산, 테마 반영)",
              "activities": ["추천 활동1", "추천 활동2"]
            },
            {
              "city": "두번째 도시명",
              "country": "국가명",
              "iataCode": "공항 코드",
              "reason": "추천 이유",
              "activities": ["추천 활동1", "추천 활동2"]
            },
            {
              "city": "세번째 도시명",
              "country": "국가명",
              "iataCode": "공항 코드",
              "reason": "추천 이유",
              "activities": ["추천 활동1", "추천 활동2"]
            }
          ]
        }
        
        만약 사용자의 요청이 여행과 전혀 관련이 없다면, 다음과 같이 반환하세요.
        {
          "chat_response": "죄송하지만 저는 여행 관련 질문에만 답변해 드릴 수 있어요. 여행 계획에 대해 알려주시겠어요?",
          "recommendations": []
        }
        """;
        
        return executeGeminiCall(conversationHistory, systemPromptText);
    }

    /**
     * [모드 2] 후속 질문 API 호출 (수정됨: 다중 추천 상황 고려)
     */
    public String callFollowUpApi(List<Map<String, String>> conversationHistory) {
        
        String systemPromptText = """
        당신은 '무성의 여행' 챗봇입니다.
        대화 내역(history)에 당신이 JSON 형식으로 추천한 **여러 여행지(recommendations)**가 포함되어 있습니다.
        
        **[가장 중요]** 사용자의 마지막 질문 의도를 다음 3가지 중 하나로 판단하세요.
        1. [후속 질문]: 추천받은 여행지들 중 하나에 대한 구체적 질문 (예: "첫번째 도시 날씨는 어때?", "다낭 맛집 알려줘")
        2. [신규 추천]: 이전에 추천받은 곳들 말고, 아예 새로운 곳을 원함 (예: "다른 곳 알려줘", "유럽으로 다시 추천해줘")
        3. [기타 질문]: 여행과 무관한 잡담 (예: "안녕?", "밥 먹었니?")
            
        **[응답 규칙]**
        - (의도 1: 후속 질문): 질문의 대상이 된 도시에 대해 상세하고 친절하게 답변하세요. (추천 목록에 있던 도시 정보를 활용)
        - (의도 2: 신규 추천): **절대 직접 추천하지 마세요.** "새로운 여행지를 추천받으시려면 하단의 '🔄 새로운 여행 추천받기' 버튼을 눌러 조건을 다시 선택해주세요."라고 안내하세요.
        - (의도 3: 기타 질문): "저는 여행 추천 챗봇입니다. 추천해드린 여행지에 대해 궁금한 점이 있으신가요?"라고 정중히 대화를 유도하세요.
            
        **[필수 응답 형식]**
        반드시 아래 JSON 형식으로만 답변하세요.
            
        {
          "chat_response": "여기에 [응답 규칙]에 따른 답변 텍스트 입력"
        }
        """;
        
        return executeGeminiCall(conversationHistory, systemPromptText);
    }


    /**
     * [공통] Gemini API 실제 호출 로직
     */
    private String executeGeminiCall(List<Map<String, String>> conversationHistory, String systemPromptText) {
        String fullApiUrl = apiUrl + apiKey;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        List<Map<String, Object>> contents = new java.util.ArrayList<>();

        // 1. 시스템 프롬프트
        Map<String, Object> systemTextPart = Map.of("text", systemPromptText);
        Map<String, Object> systemMessage = Map.of("role", "user", "parts", Collections.singletonList(systemTextPart));
        
        // 2. AI 기본 응답 설정 (Format 준수 유도)
        Map<String, Object> modelResponsePart = Map.of("text", "네, 요청하신 JSON 형식(recommendations 배열 포함)으로만 정확하게 답변하겠습니다.");
        Map<String, Object> modelResponse = Map.of("role", "model", "parts", Collections.singletonList(modelResponsePart));
        
        contents.add(systemMessage);
        contents.add(modelResponse);

        // 3. 대화 내역 추가
        List<Map<String, Object>> userConversation = conversationHistory.stream()
                .map(message -> {
                    String role = "user".equalsIgnoreCase(message.get("sender")) ? "user" : "model";
                    Map<String, Object> textPart = Map.of("text", message.get("content"));
                    return Map.of("role", role, "parts", Collections.singletonList(textPart));
                })
                .collect(Collectors.toList());
        
        contents.addAll(userConversation);

        // 4. 요청 본문
        Map<String, Object> requestBody = Map.of(
                "contents", contents
        );
        
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        // 5. API 호출
        try {
            Map<String, Object> response = restTemplate.postForObject(fullApiUrl, requestEntity, Map.class);

            String rawText = extractTextFromResponse(response);
            // 마크다운 제거 로직
            if (rawText.startsWith("```json")) {
                rawText = rawText.substring(7, rawText.length() - 3).trim();
            } else if (rawText.startsWith("```")) { 
                rawText = rawText.substring(3, rawText.length() - 3).trim();
            } else if (rawText.startsWith("`")) {
                 rawText = rawText.substring(1, rawText.length() - 1).trim();
            }
            return rawText; 

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Gemini API 호출 중 오류 발생: " + e.getMessage());
            // [수정됨] 에러 발생 시에도 프론트엔드 형식에 맞는 JSON 반환
            return """
            {
              "chat_response": "죄송합니다, 잠시 후 다시 시도해주세요. (서버 연결 오류)",
              "recommendations": []
            }
            """;
        }
    }

    // (Private) 응답 텍스트 추출 (기존 로직 유지)
    private String extractTextFromResponse(Map<String, Object> response) {
        try {
            if (response == null) {
                return "{\"chat_response\": \"API 응답 없음\", \"recommendations\": []}";
            }
            if (response.containsKey("error")) {
                Map<String, Object> error = (Map<String, Object>) response.get("error");
                String message = (String) error.get("message");
                return "{\"chat_response\": \"API 에러 발생: " + message.replace("\"", "'") + "\", \"recommendations\": []}";
            }
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                return (String) parts.get(0).get("text");
            }
        } catch (Exception e) {
             e.printStackTrace(); 
        }
        return "{\"chat_response\": \"응답 처리 실패\", \"recommendations\": []}";
    }
}