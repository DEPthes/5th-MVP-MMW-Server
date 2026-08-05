package com.wvw.mmw.domain.interview.controller;

import com.wvw.mmw.domain.interview.dto.CreateInterviewSessionRequest;
import com.wvw.mmw.domain.interview.dto.InterviewSessionStartResponse;
import com.wvw.mmw.domain.interview.service.InterviewSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 면접 세션 관련 엔드포인트.
 */
@RestController
@RequestMapping("/interviews")
@RequiredArgsConstructor
public class InterviewSessionController {

    private final InterviewSessionService interviewSessionService;

    /**
     * 면접 세션을 생성하고 질문 세트를 발급함.
     *
     * @param userId  로그인 사용자 ID. TODO: JWT 인증 도입 후 인증 정보에서 추출하도록 교체
     * @param request 면접 조건
     */
    @PostMapping
    public ResponseEntity<InterviewSessionStartResponse> create(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody CreateInterviewSessionRequest request) {

        InterviewSessionStartResponse response = interviewSessionService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}