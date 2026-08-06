package com.wvw.mmw.domain.interview.repository;

import com.wvw.mmw.domain.interview.entity.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import java.util.List;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {

    // 세션의 질문을 출제 순서대로 조회.
    List<InterviewQuestion> findByInterviewSessionIdOrderBySequenceAsc(Long sessionId);

    // 미출제 질문 중 가장 앞선 것을 조회한다. 다음 질문 진행 판단에 사용.
    Optional<InterviewQuestion> findFirstByInterviewSessionIdAndAskedAtIsNullOrderBySequenceAsc(Long sessionId);

    // 출제된 질문 수. 최소 질문 수 도달 여부 판단에 사용.
    long countByInterviewSessionIdAndAskedAtIsNotNull(Long sessionId);
}