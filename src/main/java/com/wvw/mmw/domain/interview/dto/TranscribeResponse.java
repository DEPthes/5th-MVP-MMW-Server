package com.wvw.mmw.domain.interview.dto;

/**
 * STT 일괄 변환 결과.
 *
 * @param sessionId      면접 세션 ID
 * @param totalCount     변환 대상 답변 수
 * @param completedCount 변환에 성공한 답변 수
 * @param failedCount    변환에 실패한 답변 수
 */
public record TranscribeResponse(
        Long sessionId,
        int totalCount,
        int completedCount,
        int failedCount
) {
}