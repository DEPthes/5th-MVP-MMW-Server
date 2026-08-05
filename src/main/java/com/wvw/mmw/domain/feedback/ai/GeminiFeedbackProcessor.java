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
    public OverallFeedbackResponse requestOverallFeedback(Long sessionId){
        List<InterviewQuestion> questionList = interviewQuestionRepository.findByInterviewSessionIdOrderBySequenceAsc(sessionId);
//      StringBuffer로 질문,대답 정리
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < questionList.size(); i++) {
            InterviewQuestion q = questionList.get(i);
            String transcript = answerRepository.findByInterviewQuestionId(q.getId())
                    .map(Answer::getTranscript)
                    .orElse("(답변 없음)");

            sb.append(String.format("Q%d: %s\nA%d: %s\n\n",
                    i + 1, q.getContent(),
                    i + 1, transcript != null ? transcript : "(답변 없음)"));
        }

        String prompt = String.format(
                "너는 철저하고 객관적인 AI 모의면접관이야. 지원자의 전체 면접 Q&A 내역을 분석하여 공정하게 평가해줘.\n" +
                        "전체 면접 내역:\n%s\n\n" +
                        "[평가 지침]\n" +
                        "- 아래 JSON의 점수 필드에는 0부터 100 사이의 정수를 지원자의 실제 역량에 맞게 부여할 것.\n" +
                        "- 프롬프트에 있는 예시 숫자는 형식을 보여주기 위한 것일 뿐이므로, 절대 이 숫자에 구애받지 말고 0점부터 100점까지 넓은 스펙트럼에서 냉정하게 평가할 것.\n\n" +
                        "결과는 반드시 아래 JSON 형식으로만 반환해. 다른 말은 절대 하지 마.\n" +
                        "{\n" +
                        "  \"totalScore\": 0,\n" +
                        "  \"overallSummary\": \"지원자에 대한 객관적인 종합 한줄평\",\n" +
                        "  \"thinkingScore\": 0,\n" +
                        "  \"executionScore\": 0,\n" +
                        "  \"collaborationScore\": 0,\n" +
                        "  \"growthScore\": 0,\n" +
                        "  \"adaptabilityScore\": 0,\n" +
                        "  \"fitExperienceScore\": 0,\n" +
                        "  \"fitJobUnderstandingScore\": 0,\n" +
                        "  \"fitOrganizationScore\": 0,\n" +
                        "  \"feedbackPoints\": [\n" +
                        "    {\"type\": \"STRENGTH\", \"title\": \"강점 제목\", \"description\": \"상세 설명\", \"sequence\": 1},\n" +
                        "    {\"type\": \"WEAKNESS\", \"title\": \"약점 제목\", \"description\": \"상세 설명\", \"sequence\": 1}\n" +
                        "  ]\n" +
                        "}",
                sb.toString()
        );

        String response = geminiClient.generate(prompt);

        try{
            return objectMapper.readValue(response, OverallFeedbackResponse.class);
        }catch (Exception e){
            throw new RuntimeException("Failed AI OverallFeedback parsing", e);
        }




    }

}
