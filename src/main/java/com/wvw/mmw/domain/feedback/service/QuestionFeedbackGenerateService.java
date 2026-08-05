package com.wvw.mmw.domain.feedback.service;

import com.wvw.mmw.domain.feedback.ai.GeminiFeedbackProcessor;
import com.wvw.mmw.domain.feedback.dto.OverallFeedbackResponse;
import com.wvw.mmw.domain.feedback.dto.QuestionFeedbackResponse;
import com.wvw.mmw.domain.feedback.entity.QuestionFeedback;
import com.wvw.mmw.domain.feedback.repository.FeedbackPointRepository;
import com.wvw.mmw.domain.feedback.repository.QuestionFeedbackRepository;
import com.wvw.mmw.domain.interview.entity.Answer;
import com.wvw.mmw.domain.interview.entity.InterviewQuestion;
import com.wvw.mmw.domain.interview.repository.InterviewQuestionRepository;
import com.wvw.mmw.domain.interview.repository.AnswerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// 질문 한개에 대한 피드백 생성
@Service
@RequiredArgsConstructor
public class QuestionFeedbackGenerateService {

    private final QuestionFeedbackRepository questionFeedbackRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final AnswerRepository answerRepository;

    private final GeminiFeedbackProcessor geminiFeedbackProcessor;


//  이미 생성된 피드백이 있으면 불러오고 없으면 생성
    @Transactional
    public QuestionFeedbackResponse getQuestionFeedback(Long sessionId, Long questionId){

//      findByInterviewQuestionId가 Optional이여서 map과 orElseGet 사용 가능
        return questionFeedbackRepository.findByInterviewQuestionId(questionId)
                .map(QuestionFeedbackResponse::from)//존재할 경우 DTO로 반환
                .orElseGet(()->generateQuestionFeedback(sessionId,questionId));//없으면 생성
    }

//  답변 생성
    private QuestionFeedbackResponse generateQuestionFeedback(Long sessionId, Long questionId){

//       질문 조회
        InterviewQuestion interviewQuestion = interviewQuestionRepository.findById(questionId)
                .orElseThrow(()->new IllegalArgumentException("questionId 검색 불가"));

//       답변 조회
        Answer answer = answerRepository.findByInterviewQuestionId(questionId)
                .orElseThrow(()->new IllegalArgumentException("답변 검색 불가"));

//      Gemini한테 질문과 대답으로 피드백 생성
        QuestionFeedbackResponse result = geminiFeedbackProcessor.requestFeedback(interviewQuestion.getContent(), answer.getTranscript());

//      피드백 결과로 엔티티 변환
        QuestionFeedback feedback = QuestionFeedback.builder()
                .interviewQuestion(interviewQuestion)
                .rationale(result.rationale())
                .improvedAnswer(result.improvedAnswer())
                .followUpQuestion(result.followUpQuestion())
                .build();

//      db저장
        questionFeedbackRepository.save(feedback);

        return QuestionFeedbackResponse.from(feedback);

    }
}
