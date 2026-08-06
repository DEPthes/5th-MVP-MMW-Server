package com.wvw.mmw.domain.feedback.service;

import com.wvw.mmw.domain.feedback.ai.GeminiFeedbackProcessor;
import com.wvw.mmw.domain.feedback.dto.OverallFeedbackResponse;
import com.wvw.mmw.domain.feedback.entity.FeedbackPoint;
import com.wvw.mmw.domain.feedback.entity.OverallFeedback;
import com.wvw.mmw.domain.feedback.repository.FeedbackPointRepository;
import com.wvw.mmw.domain.feedback.repository.OverallFeedbackRepository;
import com.wvw.mmw.domain.interview.entity.InterviewSession;
import com.wvw.mmw.domain.interview.entity.SessionStatus;
import com.wvw.mmw.domain.interview.repository.InterviewQuestionRepository;
import com.wvw.mmw.domain.interview.repository.InterviewSessionRepository;
import com.wvw.mmw.global.exception.BusinessException;
import com.wvw.mmw.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

//면접 종료 및 종합 피드백 생성
@Service
@RequiredArgsConstructor
public class OverallFeedbackGenerateService {

    private final InterviewSessionRepository interviewSessionRepository;
    private final GeminiFeedbackProcessor geminiFeedbackProcessor;
    private final OverallFeedbackRepository overallFeedbackRepository;
    private final FeedbackPointRepository feedbackPointRepository;


    @Transactional
    public void completeSession(Long sessionId){
        InterviewSession session = interviewSessionRepository.findById(sessionId)
                .orElseThrow(()->new BusinessException(ErrorCode.INTERVIEW_SESSION_NOT_FOUND));

        if(session.getStatus()== SessionStatus.COMPLETED){
            throw new BusinessException(ErrorCode.INTERVIEW_ALREADY_COMPLETED);
        }

//        session status completed로 변경
    }

    @Transactional
    public OverallFeedbackResponse generateOverallFeedback(Long sessionId){

        InterviewSession session = interviewSessionRepository.findById(sessionId)
                .orElseThrow(()->new BusinessException(ErrorCode.INTERVIEW_SESSION_NOT_FOUND));

        if(session.getStatus()!= SessionStatus.COMPLETED){
            throw new BusinessException(ErrorCode.INTERVIEW_NOT_COMPLETED);
        }

        if(overallFeedbackRepository.findByInterviewSessionId(sessionId).isPresent()){
            throw new BusinessException(ErrorCode.FEEDBACK_ALREADY_EXIST);
        }

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

        if (result.feedbackPoints() != null && !result.feedbackPoints().isEmpty()){
            List<FeedbackPoint> feedbackPointList = result.feedbackPoints()
                    .stream().map(dto->
                            FeedbackPoint.builder()
                                    .overallFeedback(overallFeedback)
                                    .type(dto.type())
                                    .title(dto.title())
                                    .description(dto.description())
                                    .sequence(dto.sequence())
                                    .build()
                    ).toList();

            feedbackPointRepository.saveAll(feedbackPointList);
        }
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
                overallFeedback.getFitOrganizationScore(),
                result.feedbackPoints()
        );

    }
}
