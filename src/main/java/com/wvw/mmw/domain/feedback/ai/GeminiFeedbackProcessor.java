package com.wvw.mmw.domain.feedback.ai;

import com.wvw.mmw.domain.feedback.dto.OverallFeedbackResponse;
import com.wvw.mmw.domain.feedback.dto.QuestionFeedbackResponse;
import com.wvw.mmw.domain.interview.entity.Answer;
import com.wvw.mmw.domain.interview.entity.InterviewQuestion;
import com.wvw.mmw.domain.interview.repository.AnswerRepository;
import com.wvw.mmw.domain.interview.repository.InterviewQuestionRepository;
import com.wvw.mmw.domain.interview.repository.InterviewSessionRepository;
import com.wvw.mmw.gemini.GeminiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

//
@Component
@RequiredArgsConstructor
public class GeminiFeedbackProcessor {

    private final GeminiClient geminiClient;
//  Json-> Object https://mangkyu.tistory.com/223
    private final ObjectMapper objectMapper;
    private final InterviewSessionRepository interviewSessionRepository;
    private final AnswerRepository answerRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;


    public QuestionFeedbackResponse requestFeedback(String question, String answer){
//      프롬포트
        String prompt = String.format(
                "너는 AI 모의면접관이야 질문과 지원자의 답변을 분석해서 피드백을 작성해줘 \n" +
                "질문: %s\n" +
                "답변: %s\n" +
                "결과는 반드시 아래 JSON 형식으로만 반환해. 다른 말은 절대 추가하지 마.\n" +
                "{\n" +
                "  \"rationale\": \"평가 근거 (지원자 답변의 장단점)\",\n" +
                "  \"improvedAnswer\": \"개선된 예시 모범 답변\",\n" +
                "  \"followUpQuestion\": \"이 답변에 이어질 수 있는 압박/심화 꼬리 질문\"\n" +
                "}",
                question, answer
        );
//      답변 생성
        String response = geminiClient.generate(prompt);

//       parsing 과정
        try{

            return objectMapper.readValue(response, QuestionFeedbackResponse.class);

        }catch (Exception e){
//            log.error("Failed Gemini Parsing, 원본 : {}",response,e);
            throw new RuntimeException("Failed AI Feedback parsing");
        }

    }

}
