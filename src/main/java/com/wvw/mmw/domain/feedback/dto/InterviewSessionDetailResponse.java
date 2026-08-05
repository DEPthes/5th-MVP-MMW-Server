package com.wvw.mmw.domain.feedback.dto;

import com.wvw.mmw.domain.profile.entity.CareerLevel;

import java.util.List;

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
