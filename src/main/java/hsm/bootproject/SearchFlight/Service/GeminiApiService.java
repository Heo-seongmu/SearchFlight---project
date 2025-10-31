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
    
    // (모델명은 1.5-flash 또는 2.5-flash 등 사용 가능한 모델로 지정하세요)
    private final String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";


    public GeminiApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * [모드 1] 여행지 추천 API 호출 (수정 없음)
     */
    public String callRecommendationApi(List<Map<String, String>> conversationHistory) {
        
        String systemPromptText = """
        당신은 '규아의 여행'이라는 이름의 전문 여행 추천 챗봇입니다.
        모든 답변은 한국어로, 여행 전문가의 말투로 존댓말을 사용해야 합니다.
        
        사용자는 [선택 조건]과 [사용자 추가 요청] 형식으로 프롬프트를 전달할 것입니다.
        [선택 조건]에 있는 '출발지', '출발 날짜', '지역', '테마', '기간', '경비' 6가지 요소를 **모두** 고려하여
        가장 적합한 여행지 1곳을 추천해주세요. 
        
        **[가장 중요]** '출발 날짜'를 보고 해당 여행지의 **계절적 특징(날씨, 축제, 성수기/비수기 등)**을 **반드시** 고려하여 추천해야 합니다.
        
        **[매우 중요]** 당신의 답변은 **반드시** 아래와 같은 JSON 형식이어야 합니다.
        JSON 객체 외에 다른 텍스트를 절대 포함하지 마세요.
        
        {
          "city": "추천 도시명 (예: 프랑스 파리)",
          "country": "국가명 (예: 프랑스)",
          "iataCode": "추천 도시의 3자리 IATA 공항 코드 (예: 'CDG' 또는 'PAR')",
          "reason": "이 도시을 추천하는 2-3줄의 간결한 이유. (출발 날짜의 계절, 기간, 경비 등을 반영할 것)",
          "activities": [
            "추천 활동 또는 명소 1 (계절에 맞는 활동)",
            "추천 활동 또는 명소 2",
            "추천 활동 또는 명소 3"
          ],
          "chat_response": "사용자에게 보여줄 친절한 응답 메시지. (예: '인천(ICN) 출발, 12월 10일 (겨울) 일정...')"
        }
        
        만약 사용자의 요청이 여행과 전혀 관련이 없다면, 다음과 같은 JSON을 반환하세요.
        {
          "city": "N/A",
          "country": "N/A",
          "iataCode": "N/A",
          "reason": "여행과 관련 없는 질문",
          "activities": [],
          "chat_response": "죄송하지만 저는 여행 관련 질문에만 답변해 드릴 수 있어요. 어떤 여행을 원하시는지 알려주시겠어요?"
        }
        """;
        
        return executeGeminiCall(conversationHistory, systemPromptText);
    }

    /**
     * [모드 2] 후속 질문 API 호출 (신규)
     * - [수정됨] 사용자의 의도를 파악하여 3가지 시나리오로 분기하도록 프롬프트 수정
     */
    public String callFollowUpApi(List<Map<String, String>> conversationHistory) {
        
        String systemPromptText = """
        당신은 '규아의 여행'이라는 이름의 전문 여행 추천 챗봇입니다.
        대화 내역(history)에 당신이 JSON 형식으로 추천한 여행지가 포함되어 있습니다.
        
        **[가장 중요]** 사용자의 마지막 질문 의도를 다음 3가지 중 하나로 판단하세요.
        1. [후속 질문]: 이전에 추천받은 여행지(대화 내역 속 'city')에 대한 추가 질문 (예: "거기 날씨 어때요?", "맛집 알려줘")
        2. [신규 추천]: 이전에 추천받은 여행지와 관계없이, 새로운 여행지를 추천해 달라는 요청 (예: "다른 곳 알려줘", "유럽으로 다시 추천해줘")
        3. [기타 질문]: 여행과 관계 없거나, 챗봇의 기능과 관련 없는 일반 질문 (예: "안녕?", "너는 누구야?")
            
        **[응답 규칙]**
        - (의도 1: 후속 질문인 경우): 사용자의 질문에 대해 친절하고 상세하게, 자연스러운 대화체(존댓말)로 답변해주세요.
        - (의도 2: 신규 추천인 경우): **절대 새로운 장소를 추천하지 마세요.** 대신, "물론이죠. 새로운 여행지를 추천받으시려면 상단의 '🔄 새로운 여행 추천받기' 버튼을 눌러 조건을 다시 선택해주세요." 라고 정확히 안내하는 답변을 하세요.
        - (의도 3: 기타 질문인 경우): "저는 여행 추천 챗봇입니다. 이전에 추천해드린 'OOO(도시명)'에 대해 더 궁금한 점이 있으신가요?"라고 답변하며 대화를 유도하세요. (도시명은 대화 내역을 참고)
            
        **[매우 중요]** 당신의 답변은 **반드시** 아래와 같은 JSON 형식이어야 합니다.
        JSON 객체 외에 다른 텍스트를 절대 포함하지 마세요.
            
        {
          "chat_response": "여기에 [응답 규칙]에 따른 답변을 입력하세요."
        }
        """;
        
        return executeGeminiCall(conversationHistory, systemPromptText);
    }


    /**
     * [공통] Gemini API 실제 호출 로직 (수정 없음)
     */
    private String executeGeminiCall(List<Map<String, String>> conversationHistory, String systemPromptText) {
        String fullApiUrl = apiUrl + apiKey;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        List<Map<String, Object>> contents = new java.util.ArrayList<>();

        // 1. 시스템 프롬프트 설정
        Map<String, Object> systemTextPart = Map.of("text", systemPromptText);
        Map<String, Object> systemMessage = Map.of("role", "user", "parts", Collections.singletonList(systemTextPart));
        
        // 2. 시스템 프롬프트에 대한 AI의 기본 응답 설정
        Map<String, Object> modelResponsePart = Map.of("text", "네, 알겠습니다. 지금부터 요청하신 JSON 형식으로만 답변하겠습니다.");
        Map<String, Object> modelResponse = Map.of("role", "model", "parts", Collections.singletonList(modelResponsePart));
        
        contents.add(systemMessage);
        contents.add(modelResponse);

        // 3. 실제 대화 내역 추가
        List<Map<String, Object>> userConversation = conversationHistory.stream()
                .map(message -> {
                    String role = "user".equalsIgnoreCase(message.get("sender")) ? "user" : "model";
                    Map<String, Object> textPart = Map.of("text", message.get("content"));
                    return Map.of("role", role, "parts", Collections.singletonList(textPart));
                })
                .collect(Collectors.toList());
        
        contents.addAll(userConversation);

        // 4. 요청 본문 생성
        Map<String, Object> requestBody = Map.of(
                "contents", contents
        );
        
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        // 5. API 호출
        try {
            Map<String, Object> response = restTemplate.postForObject(fullApiUrl, requestEntity, Map.class);

            String rawText = extractTextFromResponse(response);
            // 마크다운 블록(`...`) 제거
            if (rawText.startsWith("```json")) {
                rawText = rawText.substring(7, rawText.length() - 3).trim();
            } else if (rawText.startsWith("`")) {
                 rawText = rawText.substring(1, rawText.length() - 1).trim();
            }
            return rawText; 

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Gemini API 호출 중 오류 발생: " + e.getMessage());
            // API 오류 시 클라이언트가 파싱할 수 있는 공통 에러 JSON 반환
            return """
            {
              "city": "N/A",
              "country": "N/A",
              "iataCode": "N/A",
              "reason": "API 오류 발생",
              "activities": [],
              "chat_response": "죄송합니다, 지금은 답변을 드릴 수 없어요. (API 오류). 잠시 후 다시 시도해주세요."
            }
            """;
        }
    }

    // (Private) 응답 텍스트 추출 (수정 없음)
    private String extractTextFromResponse(Map<String, Object> response) {
        try {
            if (response == null) {
                return "{\"chat_response\": \"API로부터 응답을 받지 못했습니다.\"}";
            }
            if (response.containsKey("error")) {
                Map<String, Object> error = (Map<String, Object>) response.get("error");
                String message = (String) error.get("message");
                if (message.contains("overloaded")) {
                    return "{\"chat_response\": \"현재 요청이 많아 답변이 지연되고 있습니다. 잠시 후 다시 시도해주세요.\"}";
                }
                return "{\"chat_response\": \"API 에러: " + message.replace("\"", "'") + "\"}";
            }
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                return (String) parts.get(0).get("text");
            }
        } catch (Exception e) {
             e.printStackTrace(); 
             System.err.println("API 응답 파싱 중 오류: " + e.getMessage());
        }
        return "{\"chat_response\": \"응답을 처리하는 중 문제가 발생했습니다.\"}";
    }
}