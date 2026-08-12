package com.wvw.mmw.domain.interview.service;

import com.wvw.mmw.domain.interview.dto.AnswerSubmitResponse;
import com.wvw.mmw.domain.interview.entity.Answer;
import com.wvw.mmw.domain.interview.entity.AnswerStatus;
import com.wvw.mmw.domain.interview.entity.InterviewQuestion;
import com.wvw.mmw.domain.interview.repository.AnswerRepository;
import com.wvw.mmw.domain.interview.repository.InterviewQuestionRepository;
import com.wvw.mmw.domain.interview.repository.InterviewSessionRepository;
import com.wvw.mmw.global.exception.BusinessException;
import com.wvw.mmw.global.exception.ErrorCode;
import com.wvw.mmw.storage.GcsClient;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 면접 답변 음성 제출을 담당.
 *
 * <p>음성 파일을 GCS에 올리고 답변을 생성한다.
 * STT 변환은 면접 종료 후 일괄 처리하므로 여기서는 수행하지 않는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerService {

    // 답변 음성 파일의 최대 크기(바이트). 약 5분 분량.
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private static final String AUDIO_CONTENT_TYPE_PREFIX = "audio/";

    private final InterviewSessionRepository sessionRepository;
    private final InterviewQuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final GcsClient gcsClient;

    /**
     * 답변 음성을 업로드하고 답변을 생성한다.
     *
     * @param userId     요청한 사용자 ID
     * @param sessionId  면접 세션 ID
     * @param questionId 답변할 질문 ID
     * @param audioFile  답변 음성 파일
     */
    @Transactional
    public AnswerSubmitResponse submit(Long userId, Long sessionId, Long questionId,
                                       MultipartFile audioFile) {
        verifySessionOwner(userId, sessionId);
        validateFile(audioFile);

        InterviewQuestion question = questionRepository
                .findByIdAndInterviewSessionId(questionId, sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));

        if (answerRepository.existsByInterviewQuestionId(questionId)) {
            throw new BusinessException(ErrorCode.ANSWER_ALREADY_SUBMITTED);
        }

        question.markAsked();

        String objectName = uploadAudio(sessionId, questionId, audioFile);

        Answer answer = Answer.builder()
                .interviewQuestion(question)
                .status(AnswerStatus.NOT_ANSWERED)
                .build();
        answer.markUploaded(objectName);

        return AnswerSubmitResponse.from(answerRepository.save(answer));
    }

    // 세션이 본인 소유인지 확인.
    private void verifySessionOwner(Long userId, Long sessionId) {
        if (sessionRepository.findByIdAndUserId(sessionId, userId).isEmpty()) {
            throw new BusinessException(ErrorCode.SESSION_NOT_FOUND);
        }
    }

    /**
     * 업로드된 파일을 검증한다.
     * 오디오 스펙(LINEAR16 / 16000Hz / 모노)은 프론트와 합의한 값으로,
     * 형식이 어긋나면 STT 단계에서 실패로 처리된다.
     */
    private void validateFile(MultipartFile audioFile) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (audioFile.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        String contentType = audioFile.getContentType();
        if (contentType == null || !contentType.startsWith(AUDIO_CONTENT_TYPE_PREFIX)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    // 음성 파일을 GCS에 올리고 저장 경로를 반환.
    private String uploadAudio(Long sessionId, Long questionId, MultipartFile audioFile) {
        String objectName = "answers/%d/%d.wav".formatted(sessionId, questionId);

        try {
            gcsClient.upload(objectName, audioFile.getBytes(), audioFile.getContentType());
            return objectName;

        } catch (IOException e) {
            log.error("답변 음성 업로드 실패. sessionId={}, questionId={}", sessionId, questionId, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}