package com.wvw.mmw.domain.interview.entity;

import com.wvw.mmw.domain.profile.entity.ApplicationProfile;
import com.wvw.mmw.domain.profile.entity.CareerLevel;
import com.wvw.mmw.domain.user.entity.User;
import com.wvw.mmw.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "interview_sessions",
        indexes = @Index(
                name = "idx_interview_sessions_user_id_created_at",
                columnList = "user_id, created_at")
)
public class InterviewSession extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "application_profile_id")
    private ApplicationProfile applicationProfile;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @Column(name = "job_position", nullable = false, length = 100)
    private String jobPosition;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "career_level", nullable = false, length = 30)
    private CareerLevel careerLevel;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "interview_type", nullable = false, length = 30)
    private InterviewType interviewType;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private SessionStatus status;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Builder
    private InterviewSession(User user, ApplicationProfile applicationProfile, String companyName,
                             String jobPosition, CareerLevel careerLevel,
                             InterviewType interviewType, int durationMinutes,
                             SessionStatus status) {
        this.user = user;
        this.applicationProfile = applicationProfile;
        this.companyName = companyName;
        this.jobPosition = jobPosition;
        this.careerLevel = careerLevel;
        this.interviewType = interviewType;
        this.durationMinutes = durationMinutes;
        this.status = status;
    }

    // 면접 시작. 시작 시각을 기록하고 진행 중 상태로 전환.
    public void start() {
        this.startedAt = LocalDateTime.now();
        this.status = SessionStatus.IN_PROGRESS;
    }

    // 면접 종료. 종료 시각을 기록하고 분석 대기 상태로 전환.
    public void endInterview() {
        this.endedAt = LocalDateTime.now();
        this.status = SessionStatus.ANALYZING;
    }

    // 피드백 생성 완료 기록.
    public void completeAnalysis() {
        this.status = SessionStatus.COMPLETED;
    }

    // 분석에 실패했음을 사유와 함께 기록.
    public void failAnalysis(String reason) {
        this.status = SessionStatus.FAILED;
        this.failureReason = reason;
    }
}