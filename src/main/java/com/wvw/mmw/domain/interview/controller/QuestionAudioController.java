package com.wvw.mmw.domain.interview.controller;

import com.wvw.mmw.domain.interview.dto.QuestionAudioResponse;
import com.wvw.mmw.domain.interview.service.QuestionAudioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 면접 질문의 TTS 음성 관련 엔드포인트.
 */
@RestController
@RequestMapping("/interviews/{sessionId}/questions/{questionId}")
@RequiredArgsConstructor
public class QuestionAudioController {

    private final QuestionAudioService questionAudioService;

    /**
     * 질문 음성에 접근할 수 있는 서명 URL을 발급함.
     *
     * @param userId     로그인 사용자 ID. TODO: JWT 인증 도입 후 인증 정보에서 추출하도록 교체
     * @param sessionId  면접 세션 ID
     * @param questionId 질문 ID
     */
    @GetMapping("/audio")
    public ResponseEntity<QuestionAudioResponse> getAudio(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long sessionId,
            @PathVariable Long questionId) {

        return ResponseEntity.ok(questionAudioService.getAudio(userId, sessionId, questionId));
    }
}