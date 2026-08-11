package com.wvw.mmw.domain.interview.service;

import com.wvw.mmw.domain.interview.dto.TranscribeResponse;
import com.wvw.mmw.domain.interview.entity.Answer;
import com.wvw.mmw.domain.interview.entity.AnswerStatus;
import com.wvw.mmw.domain.interview.entity.InterviewSession;
import com.wvw.mmw.domain.interview.entity.SessionStatus;
import com.wvw.mmw.domain.interview.repository.AnswerRepository;
import com.wvw.mmw.domain.interview.repository.InterviewSessionRepository;
import com.wvw.mmw.global.exception.BusinessException;
import com.wvw.mmw.global.exception.ErrorCode;
import com.wvw.mmw.storage.GcsClient;
import com.wvw.mmw.stt.SttClient;
import com.wvw.mmw.stt.SttResult;
import com.wvw.mmw.stt.VoiceMetrics;
import com.wvw.mmw.stt.VoiceMetricsCalculator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 면접 종료 후 답변 음성을 일괄로 텍스트 변환.
 *
 * <p>답변마다 STT를 호출하므로 답변 수에 비례해 시간이 걸린다.
 * 개별 답변이 실패해도 나머지는 계속 처리하고, 실패한 답변만 FAILED로 남긴다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerTranscribeService {

    private final InterviewSessionRepository sessionRepository;
    private final AnswerRepository answerRepository;
    private final SttClient sttClient;
    private final VoiceMetricsCalculator voiceMetricsCalculator;
    private final GcsClient gcsClient;

    /**
     * 세션의 업로드된 답변을 모두 텍스트로 변환.
     *
     * @param userId    요청한 사용자 ID
     * @param sessionId 면접 세션 ID
     */
    @Transactional
    public TranscribeResponse transcribeAll(Long userId, Long sessionId) {
        InterviewSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        if (session.getStatus() != SessionStatus.ANALYZING) {
            throw new BusinessException(ErrorCode.INTERVIEW_NOT_ANALYZING);
        }

        List<Answer> answers = answerRepository
                .findByInterviewQuestionInterviewSessionIdAndStatus(sessionId, AnswerStatus.UPLOADED);

        int completed = 0;
        for (Answer answer : answers) {
            if (transcribe(answer)) {
                completed++;
            }
        }

        return new TranscribeResponse(sessionId, answers.size(), completed, answers.size() - completed);
    }

    /**
     * 답변 하나를 변환.
     *
     * @return 성공 여부. 실패 시 답변 상태를 FAILED로 바꾼다.
     */
    private boolean transcribe(Answer answer) {
        answer.markProcessing();

        try {
            SttResult sttResult = sttClient.transcribe(gcsClient.toGcsUri(answer.getAudioObjectPath()));
            VoiceMetrics metrics = voiceMetricsCalculator.calculate(sttResult);

            answer.completeTranscription(
                    sttResult.getTranscript(),
                    metrics.getWordCount(),
                    metrics.getSpeechDurationMs(),
                    metrics.getSilenceTotalMs(),
                    metrics.getLongestSilenceMs()
            );
            return true;

        } catch (Exception e) {
            log.error("답변 음성 변환 실패. answerId={}, path={}",
                    answer.getId(), answer.getAudioObjectPath(), e);
            answer.markFailed();
            return false;
        }
    }
}