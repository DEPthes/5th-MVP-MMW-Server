package com.wvw.mmw.gemini.prompt;

import java.util.List;
import java.util.Map;

/**
 * 종합 피드백 생성용 프롬프트와 응답 스키마를 만든다.
 * 화면설계서 '모달 FULL'의 필수 항목을 그대로 따른다.
 */
public class FeedbackPrompt {

    private FeedbackPrompt() {
    }

    /**
     * @param companyName  지원 기업
     * @param jobPosition  지원 직무
     * @param careerLevel  경력 구분
     * @param qaText       질문과 답변을 정리한 텍스트
     * @param metricsText  음성 지표를 정리한 텍스트
     */
    public static String build(String companyName, String jobPosition, String careerLevel,
                               String qaText, String metricsText) {
        return """
                당신은 %s의 %s 직무 면접관입니다.
                %s 지원자의 모의 면접 답변을 평가해주세요.

                [질문과 답변]
                %s

                [음성 분석 지표]
                %s

                평가 기준:
                - 각 점수는 0~100 사이의 정수입니다.
                - 세부 역량 5가지: 사고력, 실행력, 협업력, 성장력, 정착력
                - 포지션 FIT 3가지: 업무경험 유사도, 직무 이해도, 조직 적합도
                - 강점과 약점은 각각 2~3개씩, 키워드와 설명을 함께 제시합니다.
                - 음성 지표를 참고해 전달력에 대한 평가를 강점 또는 약점에 자연스럽게 포함합니다.
                - 답변에 없는 내용을 추측해서 언급하지 않습니다.
                - 합격 가능성은 언급하지 않습니다.
                """.formatted(companyName, jobPosition, careerLevel, qaText, metricsText);
    }

    // 응답 스키마. ERD overall_feedbacks 테이블 컬럼과 대응한다.
    public static Map<String, Object> schema() {
        Map<String, Object> score = Map.of("type", "INTEGER");

        return Map.of(
                "type", "OBJECT",
                "properties", Map.ofEntries(
                        Map.entry("totalScore", score),
                        Map.entry("overallSummary", Map.of("type", "STRING")),
                        Map.entry("thinkingScore", score),
                        Map.entry("executionScore", score),
                        Map.entry("collaborationScore", score),
                        Map.entry("growthScore", score),
                        Map.entry("adaptabilityScore", score),
                        Map.entry("fitExperienceScore", score),
                        Map.entry("fitJobUnderstandingScore", score),
                        Map.entry("fitOrganizationScore", score),
                        Map.entry("strengths", pointArray()),
                        Map.entry("weaknesses", pointArray())
                ),
                "required", List.of(
                        "totalScore", "overallSummary",
                        "thinkingScore", "executionScore", "collaborationScore",
                        "growthScore", "adaptabilityScore",
                        "fitExperienceScore", "fitJobUnderstandingScore", "fitOrganizationScore",
                        "strengths", "weaknesses"
                )
        );
    }

    // 강점·약점 항목 구조. title + description 쌍의 배열이다.
    private static Map<String, Object> pointArray() {
        return Map.of(
                "type", "ARRAY",
                "items", Map.of(
                        "type", "OBJECT",
                        "properties", Map.of(
                                "title", Map.of("type", "STRING"),
                                "description", Map.of("type", "STRING")
                        ),
                        "required", List.of("title", "description")
                )
        );
    }
}