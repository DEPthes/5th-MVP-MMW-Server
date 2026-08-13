package com.wvw.mmw.domain.interview.controller;

import com.wvw.mmw.domain.interview.dto.CreateInterviewSessionRequest;
import com.wvw.mmw.domain.interview.dto.InterviewSessionStartResponse;
import com.wvw.mmw.domain.interview.dto.InterviewSessionSummaryResponse;
import com.wvw.mmw.domain.interview.entity.SessionStatus;
import com.wvw.mmw.domain.interview.service.InterviewSessionService;
import com.wvw.mmw.global.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 면접 세션 관련 엔드포인트.
 */
@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
public class InterviewSessionController {

    private final InterviewSessionService interviewSessionService;

    /**
     * 면접 세션을 생성하고 질문 세트를 발급.
     *
     * @param userId  로그인 사용자 ID
     * @param request 면접 조건
     */
    @PostMapping
    public ResponseEntity<ApiResponse<InterviewSessionStartResponse>> create(
            @AuthenticationPrincipal Long userId,
            @RequestBody CreateInterviewSessionRequest request) {

        InterviewSessionStartResponse response = interviewSessionService.create(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("면접 세션이 생성되었습니다.", response));
    }

    /**
     * 면접 기록 목록을 최신순으로 조회.
     *
     * @param userId 로그인 사용자 ID
     * @param status 조회 필터. ALL이면 전체, COMPLETED면 완료된 기록만.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<InterviewSessionSummaryResponse>>> list(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "ALL") StatusFilter status) {

        List<InterviewSessionSummaryResponse> response =
                interviewSessionService.findAll(userId, status.toSessionStatus());

        return ResponseEntity.ok(ApiResponse.success("면접 기록을 조회했습니다.", response));
    }

    /**
     * 면접 기록을 삭제.
     *
     * @param userId    로그인 사용자 ID
     * @param sessionId 삭제할 세션 ID
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long sessionId) {

        interviewSessionService.delete(userId, sessionId);
        return ResponseEntity.noContent().build();
    }

    // 목록 조회 필터. 화면의 '전체 / 완료' 선택에 대응.
    public enum StatusFilter {
        ALL,
        COMPLETED;

        // 실제 조회에 쓸 세션 상태. 전체 조회면 null을 반환.
        SessionStatus toSessionStatus() {
            return this == ALL ? null : SessionStatus.COMPLETED;
        }
    }
}