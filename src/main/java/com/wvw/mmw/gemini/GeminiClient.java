package com.wvw.mmw.gemini;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Gemini API를 직접 호출하는 클라이언트
 * 연결 확인용으로, 프롬프트를 보내고 응답 텍스트를 받아옴
 */
@Component
public class GeminiClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    // application.yaml의 gemini.* 값을 주입받는다.
    public GeminiClient(
            @Value("${gemini.base-url}") String baseUrl,
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String model
    ) {
        this.restClient = RestClient.create(baseUrl);
        this.apiKey = apiKey;
        this.model = model;
    }

    // 프롬프트를 gemini에 보내고 생성된 텍스트 반환.
    public String generate(String prompt) {
        // Gemini가 요구하는 요청 형식: contents > parts > text
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        // URL 뒤에 ?key= 로 API 키를 붙여 인증.
        Map<String, Object> response = restClient.post()
                .uri("/v1beta/models/{model}:generateContent?key={key}", model, apiKey)
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        return extractText(response);
    }

    // 응답 JSON의 깊은 구조(candidates > content > parts > text)에서 실제 답변만 꺼낸다.
    //get(0) -> 여러개의 답변이 나올 시, 하나의(첫번째) 답변만 추출.
    //제미나이의 답변이 없을 시(==candidates 목록이 비어있음), 에러 발생. 해당 파일은 연결 확인용이라 예외 처리 구현 안함(개선 예정)
    //응답을 DTO로 받도록 개선 예정 (Map 캐스팅 제거)
    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> response) {
        List<Map<String, Object>> candidates =
                (List<Map<String, Object>>) response.get("candidates");
        Map<String, Object> content =
                (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, Object>> parts =
                (List<Map<String, Object>>) content.get("parts");
        return (String) parts.get(0).get("text");
    }
}