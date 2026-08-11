package com.wvw.mmw.domain.interview.repository;

import com.wvw.mmw.domain.interview.entity.InterviewSession;
import com.wvw.mmw.domain.interview.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {

    // 사용자의 면접 기록을 최신순으로 조회한다. 홈 목록·전체 목록에 사용.
    List<InterviewSession> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 특정 상태의 기록만 조회한다. 목록 화면의 '완료' 필터에 사용.
    List<InterviewSession> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, SessionStatus status);

    // 본인 소유인지 함께 검증하며 조회.
    Optional<InterviewSession> findByIdAndUserId(Long id, Long userId);
}