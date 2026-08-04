package com.wvw.mmw.gemini;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
public class GeminiTestController {

    private final GeminiClient geminiClient;

    // 스프링이 GeminiClient를 자동으로 넣어줌.
    public GeminiTestController(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
    }

    // GET /gemini/test?prompt=... 로 호출하면 gemini 응답을 그대로 반환.
    @GetMapping("/gemini/test")
    public String test(@RequestParam(defaultValue = "안녕하세요, 자기소개 해주세요") String prompt) {
        return geminiClient.generate(prompt);
    }

    /** JSON 응답 형식 확인용. 면접 질문 3개를 배열로 받는다. */
    @GetMapping(value = "/gemini/test-json", produces = "application/json; charset=UTF-8")
    public String testJson() {
        String prompt = """
                네이버 백엔드 개발자 신입 지원자를 위한 면접 질문 3개를 만들어주세요.
                각 질문은 40자 이내로 작성해주세요.
                """;

        Map<String, Object> schema = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "questions", Map.of(
                                "type", "ARRAY",
                                "items", Map.of("type", "STRING")
                        )
                ),
                "required", List.of("questions")
        );

        return geminiClient.generateJson(prompt, schema);
    }
}