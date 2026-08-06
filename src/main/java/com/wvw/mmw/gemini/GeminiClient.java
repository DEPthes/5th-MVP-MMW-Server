package com.wvw.mmw.gemini;

import com.wvw.mmw.gemini.dto.GeminiRequest;
import com.wvw.mmw.gemini.dto.GeminiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Map;

/**
 * Gemini API를 직접 호출하는 클라이언트
 * 프롬프트를 보내고 응답 텍스트를 받아옴
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
        // URL 뒤에 ?key= 로 API 키를 붙여 인증.
        // 요청/응답 JSON 구조는 GeminiRequest, GeminiResponse에서 처리.
        GeminiResponse response = restClient.post()
                .uri("/v1beta/models/{model}:generateContent?key={key}", model, apiKey)
                .body(GeminiRequest.of(prompt))
                .retrieve()
                .body(GeminiResponse.class);

        // 응답 본문 자체가 비어 있는 경우 처리.
        if (response == null) {
            throw new IllegalStateException("Gemini 응답이 비어 있습니다.");
        }

        return response.firstText();
    }

    /**
     * 지정한 스키마 형태의 JSON 응답을 받는다.
     * responseSchema로 형식을 강제하므로 마크다운이나 설명 없이 순수 JSON만 온다.
     */
    public String generateJson(String prompt, Map<String, Object> responseSchema) {
        GeminiResponse response = restClient.post()
                .uri("/v1beta/models/{model}:generateContent?key={key}", model, apiKey)
                .body(GeminiRequest.ofJson(prompt, responseSchema))
                .retrieve()
                .body(GeminiResponse.class);

        if (response == null) {
            throw new IllegalStateException("Gemini 응답이 비어 있습니다.");
        }

        return response.firstText();
    }
}