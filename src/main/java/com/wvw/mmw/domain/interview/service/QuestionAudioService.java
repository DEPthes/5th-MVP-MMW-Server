package com.wvw.mmw.domain.interview.service;

import com.wvw.mmw.domain.interview.dto.QuestionAudioResponse;
import com.wvw.mmw.domain.interview.entity.InterviewQuestion;
import com.wvw.mmw.domain.interview.repository.InterviewQuestionRepository;
import com.wvw.mmw.domain.interview.repository.InterviewSessionRepository;
import com.wvw.mmw.global.exception.BusinessException;
import com.wvw.mmw.global.exception.ErrorCode;
import com.wvw.mmw.storage.GcsClient;
import com.wvw.mmw.tts.TtsClient;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 면접 질문의 TTS 음성을 제공.
 *
 * <p>한 번 생성한 음성은 GCS에 보관하고 경로를 질문에 기록해,
 * 다시 듣기 시 TTS를 재호출하지 않는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionAudioService {

    /**
     * 서명 URL 유효 시간(분).
     * 최대 면접 시간이 20분이므로, 면접 중 다시 듣기가 가능하도록 여유를 두어 30분으로 설정했습니다.
     */
    private static final long URL_DURATION_MINUTES = 30;

    private static final String AUDIO_CONTENT_TYPE = "audio/mpeg";

    private final InterviewSessionRepository sessionRepository;
    private final InterviewQuestionRepository questionRepository;
    private final TtsClient ttsClient;
    private final GcsClient gcsClient;

    /**
     * 질문의 음성 파일에 접근할 수 있는 서명 URL을 발급.
     * 음성이 아직 없으면 생성해 GCS에 저장한 뒤 발급.
     *
     * @param userId     요청한 사용자 ID
     * @param sessionId  면접 세션 ID
     * @param questionId 질문 ID
     */
    @Transactional
    public QuestionAudioResponse getAudio(Long userId, Long sessionId, Long questionId) {
        verifySessionOwner(userId, sessionId);

        InterviewQuestion question = questionRepository
                .findByIdAndInterviewSessionId(questionId, sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));

        String objectName = (question.getTtsObjectPath() != null)
                ? question.getTtsObjectPath()
                : createAudio(question);

        String signedUrl = gcsClient.generateSignedUrl(objectName, URL_DURATION_MINUTES);

        return new QuestionAudioResponse(
                question.getId(),
                signedUrl,
                LocalDateTime.now().plusMinutes(URL_DURATION_MINUTES)
        );
    }

    // 세션이 본인 소유인지 확인.
    private void verifySessionOwner(Long userId, Long sessionId) {
        if (sessionRepository.findByIdAndUserId(sessionId, userId).isEmpty()) {
            throw new BusinessException(ErrorCode.SESSION_NOT_FOUND);
        }
    }

    // TTS로 음성을 만들어 GCS에 올리고, 경로를 질문에 기록.
    private String createAudio(InterviewQuestion question) {
        String objectName = buildObjectName(question);

        try {
            byte[] audio = ttsClient.synthesize(question.getContent());
            gcsClient.upload(objectName, audio, AUDIO_CONTENT_TYPE);

        } catch (Exception e) {
            log.error("질문 음성 생성 실패. questionId={}", question.getId(), e);
            throw new BusinessException(ErrorCode.TTS_GENERATION_FAILED);
        }

        question.saveTtsPath(objectName);
        return objectName;
    }

    // GCS 내 저장 경로. 예: questions/15/101.mp3
    private String buildObjectName(InterviewQuestion question) {
        return "questions/%d/%d.mp3".formatted(
                question.getInterviewSession().getId(),
                question.getId()
        );
    }
}