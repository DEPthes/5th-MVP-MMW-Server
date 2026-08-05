package com.wvw.mmw.domain.interview.repository;

import com.wvw.mmw.domain.interview.entity.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {
    List<InterviewQuestion> findByInterviewSessionIdOrderBySequenceAsc(Long sessionId);
}
