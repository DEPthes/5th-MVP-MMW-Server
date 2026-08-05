package com.wvw.mmw.domain.feedback.controller;

import com.wvw.mmw.domain.feedback.dto.InterviewSessionDetailResponse;
import com.wvw.mmw.domain.feedback.service.InterviewDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.function.EntityResponse;

@RestController
@RequiredArgsConstructor
public class InterViewDetailController {

    private final InterviewDetailService interviewDetailService;

//  [기능 17, 21] 면접 기록 상세 조회
    @GetMapping("/api/v1/interviews/{sessionId}/detail")
    public ResponseEntity<InterviewSessionDetailResponse> getInterviewDetail(@PathVariable("sessionId") Long sessionId){

        InterviewSessionDetailResponse response = interviewDetailService.getInterviewDetail(sessionId);

        return ResponseEntity.ok(response);
    }
}
