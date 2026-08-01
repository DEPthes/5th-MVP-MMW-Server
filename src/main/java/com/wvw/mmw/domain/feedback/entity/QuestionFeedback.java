package com.wvw.mmw.domain.feedback.entity;

import com.wvw.mmw.domain.interview.entity.InterviewQuestion;
import com.wvw.mmw.global.entity.BaseCreatedAtEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "question_feedbacks")
public class QuestionFeedback extends BaseCreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "interview_question_id", nullable = false)
    private InterviewQuestion interviewQuestion;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rationale;

    @Column(name = "improved_answer", nullable = false, columnDefinition = "TEXT")
    private String improvedAnswer;

    @Column(name = "follow_up_question", nullable = false, columnDefinition = "TEXT")
    private String followUpQuestion;

    @Builder
    private QuestionFeedback(InterviewQuestion interviewQuestion, String rationale,
                             String improvedAnswer, String followUpQuestion) {
        this.interviewQuestion = interviewQuestion;
        this.rationale = rationale;
        this.improvedAnswer = improvedAnswer;
        this.followUpQuestion = followUpQuestion;
    }
}
