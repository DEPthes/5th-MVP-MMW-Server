package com.wvw.mmw.domain.interview.repository;

import com.wvw.mmw.domain.interview.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    Optional<Answer> findByInterviewQuestionId(Long questionId);
}
