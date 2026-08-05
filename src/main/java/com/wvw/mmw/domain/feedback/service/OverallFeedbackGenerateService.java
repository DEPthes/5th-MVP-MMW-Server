package com.wvw.mmw.domain.feedback.service;

import com.wvw.mmw.domain.feedback.ai.GeminiFeedbackProcessor;
import com.wvw.mmw.domain.feedback.dto.OverallFeedbackResponse;
import com.wvw.mmw.domain.feedback.entity.OverallFeedback;
import com.wvw.mmw.domain.feedback.repository.OverallFeedbackRepository;
import com.wvw.mmw.domain.interview.entity.InterviewSession;
import com.wvw.mmw.domain.interview.repository.InterviewQuestionRepository;
import com.wvw.mmw.domain.interview.repository.InterviewSessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

//종합 피드백 생성
@Service
@RequiredArgsConstructor
public class OverallFeedbackGenerateService {

    private final InterviewSessionRepository interviewSessionRepository;
    private final GeminiFeedbackProcessor geminiFeedbackProcessor;
    private final OverallFeedbackRepository overallFeedbackRepository;

    @Transactional
    public OverallFeedbackResponse generateOverallFeedback(Long sessionId){

        InterviewSession session = interviewSessionRepository.findById(sessionId)
                .orElseThrow(()->new IllegalArgumentException("sessionId로 검색 불가"));

        OverallFeedbackResponse result = geminiFeedbackProcessor.requestOverallFeedback(sessionId);

        OverallFeedback overallFeedback = OverallFeedback.builder()
                .interviewSession(session)
                .totalScore(result.totalScore())
                .overallSummary(result.overallSummary())
                .thinkingScore(result.thinkingScore())
                .executionScore(result.executionScore())
                .collaborationScore(result.collaborationScore())
                .growthScore(result.growthScore())
                .adaptabilityScore(result.adaptabilityScore())
                .fitExperienceScore(result.fitExperienceScore())
                .fitJobUnderstandingScore(result.fitJobUnderstandingScore())
                .fitOrganizationScore(result.fitOrganizationScore())
                .build();

        overallFeedbackRepository.save(overallFeedback);

        return new OverallFeedbackResponse(
                overallFeedback.getTotalScore(),
                overallFeedback.getOverallSummary(),
                overallFeedback.getThinkingScore(),
                overallFeedback.getExecutionScore(),
                overallFeedback.getCollaborationScore(),
                overallFeedback.getGrowthScore(),
                overallFeedback.getAdaptabilityScore(),
                overallFeedback.getFitExperienceScore(),
                overallFeedback.getFitJobUnderstandingScore(),
                overallFeedback.getFitOrganizationScore()
        );

    }
}
