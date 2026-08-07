package com.wvw.mmw.domain.feedback.repository;

import com.wvw.mmw.domain.feedback.entity.FeedbackPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackPointRepository extends JpaRepository<FeedbackPoint, Long> {

//    Sequence 오름차숨으로 정렬
    List<FeedbackPoint> findAllByOverallFeedbackIdOrderBySequenceAsc(Long overallFeedbackId);
}
