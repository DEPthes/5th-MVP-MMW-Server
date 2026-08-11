package com.wvw.mmw.domain.interview.dto;

import com.wvw.mmw.domain.interview.entity.InterviewSession;
import com.wvw.mmw.domain.interview.entity.SessionStatus;
import java.time.LocalDateTime;

// 면접 기록 목록 조회 응답. 배열의 원소 하나에 해당함.
public record InterviewSessionSummaryResponse(
        Long sessionId,
        String companyName,
        String jobPosition,
        SessionStatus status,
        LocalDateTime createdAt
) {

    public static InterviewSessionSummaryResponse from(InterviewSession session) {
        return new InterviewSessionSummaryResponse(
                session.getId(),
                session.getCompanyName(),
                session.getJobPosition(),
                session.getStatus(),
                session.getCreatedAt()
        );
    }
}