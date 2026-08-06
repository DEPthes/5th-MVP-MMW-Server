package com.wvw.mmw.gemini;

import com.wvw.mmw.gemini.prompt.FeedbackPrompt;
import com.wvw.mmw.gemini.prompt.QuestionPrompt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Gemini 연결 및 프롬프트 확인용 컨트롤러
 * 실제 기능 구현 시 제거 예정
 */
@RestController
public class GeminiTestController {

    private final GeminiClient geminiClient;

    // 스프링이 GeminiClient를 자동으로 넣어준다 (생성자 주입)
    public GeminiTestController(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
    }

    // GET /gemini/test?prompt=... 로 호출하면 Gemini 응답을 그대로 반환한다
    @GetMapping(value = "/gemini/test", produces = "application/json; charset=UTF-8")
    public String test(@RequestParam(defaultValue = "안녕하세요, 자기소개 해주세요") String prompt) {
        return geminiClient.generate(prompt);
    }

    // 질문 생성 프롬프트 확인용
    @GetMapping(value = "/gemini/test-questions", produces = "application/json; charset=UTF-8")
    public String testQuestions() {
        String prompt = QuestionPrompt.build("카카오", "백엔드 개발자", "신입", 5);
        return geminiClient.generateJson(prompt, QuestionPrompt.schema());
    }

    // 종합 피드백 프롬프트 확인용
    @GetMapping(value = "/gemini/test-feedback", produces = "application/json; charset=UTF-8")
    public String testFeedback() {
        String qaText = """
                Q1. 간단히 자기소개 부탁드립니다.
                A1. 저는 백엔드 개발자를 지원한 김우성입니다. 스프링 부트를 주로 사용했습니다.

                Q2. 가장 어려웠던 프로젝트 경험을 말씀해주세요.
                A2. 팀 프로젝트에서 외부 API 연동을 맡았는데, 문서가 부족해서 직접 테스트하며 검증했습니다.
                """;

        String metricsText = """
                - 전체 발화 시간: 14.8초
                - 어절 수: 9개
                - 3초 이상 멈춤: 1회 (최장 3.4초)
                """;

        String prompt = FeedbackPrompt.build("카카오", "백엔드 개발자", "신입", qaText, metricsText);
        return geminiClient.generateJson(prompt, FeedbackPrompt.schema());
    }
}