package com.wvw.mmw.domain.interview.repository;

import com.wvw.mmw.domain.interview.entity.Answer;
import com.wvw.mmw.domain.interview.entity.AnswerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    // 질문에 연결된 답변을 조회한다. 질문과 1:1 관계.
    Optional<Answer> findByInterviewQuestionId(Long questionId);

    // 세션 내 특정 상태의 답변을 조회한다. 면접 종료 후 STT 대상(UPLOADED) 조회에 사용한다.
    List<Answer> findByInterviewQuestionInterviewSessionIdAndStatus(Long sessionId, AnswerStatus status);

    // 세션의 모든 답변을 질문 순서대로 조회한다. 피드백 프롬프트 구성에 사용한다.
    List<Answer> findByInterviewQuestionInterviewSessionIdOrderByInterviewQuestionSequenceAsc(Long sessionId);

    // 해당 질문에 이미 답변이 있는지 확인한다.
    boolean existsByInterviewQuestionId(Long questionId);
}