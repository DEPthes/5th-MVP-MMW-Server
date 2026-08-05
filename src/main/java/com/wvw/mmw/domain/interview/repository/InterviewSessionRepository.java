package com.wvw.mmw.domain.interview.repository;

import com.wvw.mmw.domain.interview.entity.InterviewQuestion;
import com.wvw.mmw.domain.interview.entity.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
}
