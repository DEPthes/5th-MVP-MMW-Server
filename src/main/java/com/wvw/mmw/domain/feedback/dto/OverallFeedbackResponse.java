package com.wvw.mmw.domain.feedback.dto;

import com.wvw.mmw.domain.feedback.entity.FeedbackPoint;
import com.wvw.mmw.domain.feedback.entity.FeedbackPointType;

import java.util.List;

// api url에 sessionId존재
//종합피드백 반환값
public record OverallFeedbackResponse (
        int totalScore,
        String overallSummary,
        int thinkingScore,
        int executionScore,
        int collaborationScore,
        int growthScore,
        int adaptabilityScore,
        int fitExperienceScore,
        int fitJobUnderstandingScore,
        int fitOrganizationScore,
        List<FeedbackPointDTO> feedbackPoints

){
    public record FeedbackPointDTO(
        FeedbackPointType type,
        String title,
        String description,
        int sequence
    ){}
//    2개의 테이블이 필요해서 Service에서 dto진행(OverallFeedback, FeedbackPoint)
//    API에서 OverallFeedbackResponse가 2개의 테이블을 필요해함
}
