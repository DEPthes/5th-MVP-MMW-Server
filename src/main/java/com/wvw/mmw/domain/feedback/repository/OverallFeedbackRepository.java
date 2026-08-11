package com.wvw.mmw.domain.feedback.repository;

import com.wvw.mmw.domain.feedback.entity.OverallFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OverallFeedbackRepository extends JpaRepository<OverallFeedback, Long> {

//    overallFeedback이 아직 생성 안됬을 때를 고려해서 Optional
//    SELECT * FROM overall_feedback WHERE interview_session_id = :sessionId
    Optional<OverallFeedback> findByInterviewSessionId(Long sessionId);
}
