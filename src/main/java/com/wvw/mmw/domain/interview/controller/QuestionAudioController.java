package com.wvw.mmw.domain.interview.controller;

import com.wvw.mmw.domain.interview.dto.QuestionAudioResponse;
import com.wvw.mmw.domain.interview.service.QuestionAudioService;
import com.wvw.mmw.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 면접 질문의 TTS 음성 관련 엔드포인트.
 */
@RestController
@RequestMapping("/api/v1/interviews/{sessionId}/questions/{questionId}")
@RequiredArgsConstructor
public class QuestionAudioController {

    private final QuestionAudioService questionAudioService;

    /**
     * 질문 음성에 접근할 수 있는 서명 URL을 발급.
     *
     * @param userId     로그인 사용자 ID
     * @param sessionId  면접 세션 ID
     * @param questionId 질문 ID
     */
    @GetMapping("/audio")
    public ResponseEntity<ApiResponse<QuestionAudioResponse>> getAudio(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long sessionId,
            @PathVariable Long questionId) {

        QuestionAudioResponse response =
                questionAudioService.getAudio(userId, sessionId, questionId);

        return ResponseEntity.ok(ApiResponse.success("질문 음성을 조회했습니다.", response));
    }
}