package com.wvw.mmw.domain.feedback.dto;

import com.wvw.mmw.domain.profile.entity.CareerLevel;

import java.util.List;


//면접 기록 상세 조회 반환값
public record InterviewSessionDetailResponse(
        Long sessionId,
        String companyName,
        String jobPosition,
        CareerLevel careerLevel,
        int durationMinutes,
        OverallFeedbackResponse overallFeedback,
        List<QaDTO> qaList
){
    public record QaDTO(
            Long questionId,
            int sequence,
            String questionContent,
            String transcript,
            QuestionFeedbackResponse feedback
    ){}
}
