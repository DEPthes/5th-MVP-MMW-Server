package com.wvw.mmw.domain.feedback.repository;

import com.wvw.mmw.domain.feedback.entity.QuestionFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuestionFeedbackRepository extends JpaRepository<QuestionFeedback, Long> {

//    interviewQuestionId로  QuestionFeedback조회
    Optional<QuestionFeedback> findByInterviewQuestionId(Long interviewQuestionId);
}
