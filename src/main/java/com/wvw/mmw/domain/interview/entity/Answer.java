package com.wvw.mmw.domain.interview.entity;

import com.wvw.mmw.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "answers")
public class Answer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "interview_question_id", nullable = false)
    private InterviewQuestion interviewQuestion;

    @Column(name = "audio_object_path")
    private String audioObjectPath;

    @Column(columnDefinition = "TEXT")
    private String transcript;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private AnswerStatus status;

    // 어절 수. STT 전사 결과에서 산출.
    @Column(name = "word_count")
    private Integer wordCount;

    // 발화 구간의 총 길이(ms).
    @Column(name = "speech_duration_ms")
    private Long speechDurationMs;

    // 침묵 구간의 합계(ms).
    @Column(name = "silence_total_ms")
    private Long silenceTotalMs;

    // 가장 긴 침묵 구간의 길이(ms).
    @Column(name = "longest_silence_ms")
    private Long longestSilenceMs;

    @Builder
    private Answer(InterviewQuestion interviewQuestion, AnswerStatus status) {
        this.interviewQuestion = interviewQuestion;
        this.status = status;
    }

    // 음성 파일 업로드가 끝났음을 기록.
    public void markUploaded(String audioObjectPath) {
        this.audioObjectPath = audioObjectPath;
        this.status = AnswerStatus.UPLOADED;
    }

    // STT 변환을 시작했음을 기록.
    public void markProcessing() {
        this.status = AnswerStatus.PROCESSING;
    }

    // STT 변환 결과와 음성 지표를 저장.
    public void completeTranscription(String transcript, int wordCount, long speechDurationMs,
                                      long silenceTotalMs, long longestSilenceMs) {
        this.transcript = transcript;
        this.wordCount = wordCount;
        this.speechDurationMs = speechDurationMs;
        this.silenceTotalMs = silenceTotalMs;
        this.longestSilenceMs = longestSilenceMs;
        this.status = AnswerStatus.COMPLETED;
    }

    // STT 변환에 실패했음을 기록.
    public void markFailed() {
        this.status = AnswerStatus.FAILED;
    }
}