package com.wvw.mmw.domain.feedback.entity;

import com.wvw.mmw.domain.interview.entity.InterviewSession;
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
@Table(name = "overall_feedbacks")
public class OverallFeedback extends BaseCreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "interview_session_id", nullable = false)
    private InterviewSession interviewSession;

    @Column(name = "total_score", nullable = false)
    private int totalScore;

    @Column(name = "overall_summary", nullable = false, columnDefinition = "TEXT")
    private String overallSummary;

    @Column(name = "thinking_score", nullable = false)
    private int thinkingScore;

    @Column(name = "execution_score", nullable = false)
    private int executionScore;

    @Column(name = "collaboration_score", nullable = false)
    private int collaborationScore;

    @Column(name = "growth_score", nullable = false)
    private int growthScore;

    @Column(name = "adaptability_score", nullable = false)
    private int adaptabilityScore;

    @Column(name = "fit_experience_score", nullable = false)
    private int fitExperienceScore;

    @Column(name = "fit_job_understanding_score", nullable = false)
    private int fitJobUnderstandingScore;

    @Column(name = "fit_organization_score", nullable = false)
    private int fitOrganizationScore;

    @Builder
    private OverallFeedback(InterviewSession interviewSession, int totalScore,
                            String overallSummary, int thinkingScore, int executionScore,
                            int collaborationScore, int growthScore, int adaptabilityScore,
                            int fitExperienceScore, int fitJobUnderstandingScore,
                            int fitOrganizationScore) {
        this.interviewSession = interviewSession;
        this.totalScore = totalScore;
        this.overallSummary = overallSummary;
        this.thinkingScore = thinkingScore;
        this.executionScore = executionScore;
        this.collaborationScore = collaborationScore;
        this.growthScore = growthScore;
        this.adaptabilityScore = adaptabilityScore;
        this.fitExperienceScore = fitExperienceScore;
        this.fitJobUnderstandingScore = fitJobUnderstandingScore;
        this.fitOrganizationScore = fitOrganizationScore;
    }
}
