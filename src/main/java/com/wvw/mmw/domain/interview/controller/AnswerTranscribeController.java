package com.wvw.mmw.domain.interview.controller;

import com.wvw.mmw.domain.interview.dto.TranscribeResponse;
import com.wvw.mmw.domain.interview.service.AnswerTranscribeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 답변 음성 일괄 변환 엔드포인트.
 */
@RestController
@RequestMapping("/interviews/{sessionId}")
@RequiredArgsConstructor
public class AnswerTranscribeController {

    private final AnswerTranscribeService answerTranscribeService;

    /**
     * 면접 종료 후 업로드된 답변 음성을 모두 텍스트로 변환.
     * 세션이 분석 대기(ANALYZING) 상태일 때만 동작.
     *
     * @param userId    로그인 사용자 ID. TODO: JWT 인증 도입 후 인증 정보에서 추출하도록 교체
     * @param sessionId 면접 세션 ID
     */
    @PostMapping("/transcribe")
    public ResponseEntity<TranscribeResponse> transcribe(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long sessionId) {

        return ResponseEntity.ok(answerTranscribeService.transcribeAll(userId, sessionId));
    }
}