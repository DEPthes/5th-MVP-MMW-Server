package com.wvw.mmw.domain.interview.dto;

import java.time.LocalDateTime;

/**
 * 질문 TTS 음성 조회 응답.
 *
 * @param questionId 질문 ID
 * @param audioUrl   GCS 서명 URL. 만료 시각까지만 접근 가능함.
 * @param expiresAt  URL 만료 시각
 */
public record QuestionAudioResponse(
        Long questionId,
        String audioUrl,
        LocalDateTime expiresAt
) {
}