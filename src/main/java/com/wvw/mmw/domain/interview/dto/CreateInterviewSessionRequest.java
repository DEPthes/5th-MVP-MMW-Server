package com.wvw.mmw.domain.interview.dto;

import com.wvw.mmw.domain.profile.entity.CareerLevel;
import com.wvw.mmw.domain.interview.entity.InterviewType;

/**
 * 면접 세션 생성 요청.
 *
 * @param applicationProfileId 지원 프로필 ID (없으면 null)
 * @param companyName          지원 기업
 * @param jobPosition          지원 직무
 * @param careerLevel          경력 구분
 * @param interviewType        면접 유형
 * @param durationMinutes      면접 시간(분)
 */
public record CreateInterviewSessionRequest(
        Long applicationProfileId,
        String companyName,
        String jobPosition,
        CareerLevel careerLevel,
        InterviewType interviewType,
        int durationMinutes
) {
}