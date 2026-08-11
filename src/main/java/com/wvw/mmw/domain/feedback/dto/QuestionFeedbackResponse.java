package com.wvw.mmw.domain.feedback.dto;

import com.wvw.mmw.domain.feedback.entity.QuestionFeedback;

//setter가 없어서 불변성을 보장하고 컴파일러가 자동으로 생성자, 읽기 관련 메서드를 생성하여 class방식 대신 record 방식 사용
//보낼땐 request 받을 땐 response으로 이름을 설정 하고자 함
//순환참조 방지를 위해 interviewQuestion의 id를 반환
public record QuestionFeedbackResponse (
    Long questionId,
    String rationale,
    String improvedAnswer,
    String followUpQuestion

){
//    entity를 DTO로 변환(entity에서 필요한 정보만 DTO로 반환)
//    정적 팩토리 메소드로 유지 보수성 증가(캡슐화)
//    https://wonsoonge.tistory.com/23 참조
    public static QuestionFeedbackResponse from(QuestionFeedback feedback){
        return new QuestionFeedbackResponse(
                feedback.getInterviewQuestion().getId(),
                feedback.getRationale(),
                feedback.getImprovedAnswer(),
                feedback.getFollowUpQuestion()

        );
    }

}
