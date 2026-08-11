package com.wvw.mmw.gemini.prompt;

import java.util.List;
import java.util.Map;

/**
 * 면접 질문 세트 생성용 프롬프트와 응답 스키마를 만든다.
 */
public class QuestionPrompt {

    private QuestionPrompt() {
    }

    /**
     * 지원 정보를 바탕으로 질문 생성 프롬프트를 만든다.
     *
     * @param companyName 지원 기업
     * @param jobPosition 지원 직무
     * @param careerLevel 경력 구분
     * @param count       생성할 질문 개수
     */
    public static String build(String companyName, String jobPosition,
                               String careerLevel, int count) {
        return """
                당신은 %s의 %s 직무 면접관입니다.
                %s 지원자를 대상으로 모의 면접 질문 %d개를 만들어주세요.

                조건:
                - 각 질문은 40자 이내로 작성합니다.
                - 자기소개로 시작해 직무 역량, 경험, 협업 순으로 구성합니다.
                - 실제 면접에서 나올 법한 자연스러운 구어체로 작성합니다.
                - 한 질문에 하나의 주제만 담습니다.
                """.formatted(companyName, jobPosition, careerLevel, count);
    }

    // 응답 스키마. 문자열 배열 하나만 받는다.
    public static Map<String, Object> schema() {
        return Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "questions", Map.of(
                                "type", "ARRAY",
                                "items", Map.of("type", "STRING")
                        )
                ),
                "required", List.of("questions")
        );
    }
}