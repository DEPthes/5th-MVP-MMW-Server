package com.wvw.mmw.domain.feedback.controller;

import com.wvw.mmw.domain.feedback.dto.OverallFeedbackResponse;
import com.wvw.mmw.domain.feedback.service.InterviewDetailService;
import com.wvw.mmw.domain.feedback.service.OverallFeedbackGenerateService;
import com.wvw.mmw.gemini.GeminiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OverallFeedbackController {

    private final OverallFeedbackGenerateService overallFeedbackGenerateService;
    private final GeminiClient geminiClient;
    private final InterviewDetailService interviewDetailService;

    //  면접 종료
    @PostMapping("/api/v1/interviews/{sessionId}/complete")
    public ResponseEntity<Void> completeInterView(@PathVariable Long sessionId){
        overallFeedbackGenerateService.completeSession(sessionId);
        return ResponseEntity.ok().build();
    }
//    [기능 16] 종합 피드백 생성 요청
    @PostMapping("/api/v1/interviews/{sessionId}/overallFeedback")
    public ResponseEntity<OverallFeedbackResponse> generateOverallFeedback(@PathVariable Long sessionId){

        OverallFeedbackResponse response = overallFeedbackGenerateService.generateOverallFeedback(sessionId);
        return ResponseEntity.ok(response);
    }
}
