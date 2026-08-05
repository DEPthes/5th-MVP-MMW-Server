package com.wvw.mmw.domain.feedback.controller;

import com.wvw.mmw.domain.feedback.dto.QuestionFeedbackResponse;
import com.wvw.mmw.domain.feedback.entity.QuestionFeedback;
import com.wvw.mmw.domain.feedback.service.QuestionFeedbackGenerateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class QuestionFeedbackController {

    public final QuestionFeedbackGenerateService questionFeedbackGenerateService;

//  [기능 18] 질문별 온디맨드(On-Demand) AI 피드백 생성 및 조회
    /**
     * TODO: 추후 글로벌 예외 처리(RestControllerAdvice) 세팅 시 실패 케이스 반영 필요
     * - 400 Bad Request: sessionId, questionId 잘못된 형식
     * - 404 Not Found: DB에서 질문이나 사용자 답변을 찾을 수 없을 때 (IllegalArgumentException)
     * - 500 Internal Server Error: Gemini API 호출 실패 또는 JSON 파싱 오류 (RuntimeException)
     */
    @PostMapping("/api/v1/interviews/{sessionId}/questions/{questionId}/feedback")
    public ResponseEntity<QuestionFeedbackResponse> generateFeedback(@PathVariable("sessionId") Long sessionId, @PathVariable("questionId") Long questionId){
        QuestionFeedbackResponse response = questionFeedbackGenerateService.getQuestionFeedback(sessionId, questionId);

        return ResponseEntity.ok(response);
    }


}
