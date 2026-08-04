package com.wvw.mmw.domain.interview.entity;

import com.wvw.mmw.global.entity.BaseCreatedAtEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "interview_questions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_interview_questions_session_id_sequence",
                columnNames = {"interview_session_id", "sequence"})
)
public class InterviewQuestion extends BaseCreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "interview_session_id", nullable = false)
    private InterviewSession interviewSession;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int sequence;

    @Column(name = "asked_at")
    private LocalDateTime askedAt;

    @Column(name = "tts_object_path")
    private String ttsObjectPath;

    @Builder
    private InterviewQuestion(InterviewSession interviewSession, String content, int sequence) {
        this.interviewSession = interviewSession;
        this.content = content;
        this.sequence = sequence;
    }
}
