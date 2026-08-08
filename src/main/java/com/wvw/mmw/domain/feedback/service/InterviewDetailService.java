package com.wvw.mmw.domain.feedback.service;

import com.wvw.mmw.domain.feedback.dto.InterviewSessionDetailResponse;
import com.wvw.mmw.domain.feedback.dto.OverallFeedbackResponse;
import com.wvw.mmw.domain.feedback.dto.QuestionFeedbackResponse;
import com.wvw.mmw.domain.feedback.entity.FeedbackPoint;
import com.wvw.mmw.domain.feedback.entity.OverallFeedback;
import com.wvw.mmw.domain.feedback.repository.FeedbackPointRepository;
import com.wvw.mmw.domain.feedback.repository.OverallFeedbackRepository;
import com.wvw.mmw.domain.feedback.repository.QuestionFeedbackRepository;
import com.wvw.mmw.domain.interview.entity.Answer;
import com.wvw.mmw.domain.interview.entity.InterviewQuestion;
import com.wvw.mmw.domain.interview.entity.InterviewSession;
import com.wvw.mmw.domain.interview.repository.AnswerRepository;
import com.wvw.mmw.domain.interview.repository.InterviewQuestionRepository;
import com.wvw.mmw.domain.interview.repository.InterviewSessionRepository;
import com.wvw.mmw.global.exception.BusinessException;
import com.wvw.mmw.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

//면접 상세 조회
@Service
@RequiredArgsConstructor
public class InterviewDetailService {

    private final InterviewSessionRepository interviewSessionRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final FeedbackPointRepository feedbackPointRepository;
    private final OverallFeedbackRepository overallFeedbackRepository;
    private final QuestionFeedbackRepository questionFeedbackRepository;
    private final AnswerRepository answerRepository;

    @Transactional
    public InterviewSessionDetailResponse getInterviewDetail(Long sessionId){

//      interviewSession(sessionId, companyName, jobPosition, careerLevel, durationMinutes)
        InterviewSession session = interviewSessionRepository.findById(sessionId)
                .orElseThrow(()-> new BusinessException(ErrorCode.INTERVIEW_SESSION_NOT_FOUND));

//      overallFeedback
        OverallFeedback overallFeedback =overallFeedbackRepository.findByInterviewSessionId(sessionId)
                .orElseThrow(()->new BusinessException(ErrorCode.OVERALL_FEEDBACK_NOT_FOUND));

//      feedbackPoint
        List<FeedbackPoint> feedbackPointList = feedbackPointRepository.findAllByOverallFeedbackIdOrderBySequenceAsc(overallFeedback.getId());
        List<OverallFeedbackResponse.FeedbackPointDTO> feedbackPointDTOs = feedbackPointList.stream()
                .map(dto -> new OverallFeedbackResponse.FeedbackPointDTO(
                        dto.getType(),
                        dto.getTitle(),
                        dto.getDescription(),
                        dto.getSequence()
                )).toList();

//      overallFeedbackResponse로 결합
        OverallFeedbackResponse overallFeedbackResponse =  new OverallFeedbackResponse(
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
                feedbackPointDTOs
        );

//      qaList
        List<InterviewQuestion> questionList = interviewQuestionRepository.findByInterviewSessionIdOrderBySequenceAsc(sessionId);
        List<InterviewSessionDetailResponse.QaDTO> qaList = questionList.stream()
                .map(question->{
                    String transcript = answerRepository.findByInterviewQuestionId(question.getId())
                            .map(Answer::getTranscript)//transcript만 추출
                            .orElse(null);//비어있으면 null

                    QuestionFeedbackResponse questionFeedbackResponse = questionFeedbackRepository.findByInterviewQuestionId(question.getId())
                            .map(questionFeedback -> new QuestionFeedbackResponse(
                                    questionFeedback.getInterviewQuestion().getId(),
                                    questionFeedback.getRationale(),
                                    questionFeedback.getImprovedAnswer(),
                                    questionFeedback.getFollowUpQuestion()
                            )).orElse(null);

                    return new InterviewSessionDetailResponse.QaDTO(
                            question.getId(),
                            question.getSequence(),
                            question.getContent(),
                            transcript,
                            questionFeedbackResponse
                    );
                }).toList();

        return new InterviewSessionDetailResponse(
                session.getId(),
                session.getCompanyName(),
                session.getJobPosition(),
                session.getCareerLevel(),
                session.getDurationMinutes(),
                overallFeedbackResponse,
                qaList
        );


    }
}
