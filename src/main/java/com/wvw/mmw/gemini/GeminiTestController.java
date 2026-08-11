package com.wvw.mmw.gemini;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}