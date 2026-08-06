package com.wvw.mmw.domain.interview.dto;

import com.wvw.mmw.domain.interview.entity.InterviewType;

/**
 * 면접 세션 생성 요청.
 *
 * @param applicationProfileId 지원 프로필 ID. 기업·직무·경력 정보를 여기서 조회한다.
 * @param interviewType        면접 유형
 * @param durationMinutes      면접 시간(분). 5, 10, 15, 20 중 하나.
 */
public record CreateInterviewSessionRequest(
        Long applicationProfileId,
        InterviewType interviewType,
        int durationMinutes
) {
}