package com.wvw.mmw.domain.interview.controller;

import com.wvw.mmw.domain.interview.dto.AnswerSubmitResponse;
import com.wvw.mmw.domain.interview.service.AnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 면접 답변 제출 엔드포인트.
 */
@RestController
@RequestMapping("/interviews/{sessionId}/answers")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    /**
     * 답변 음성을 제출한다. 파일은 GCS에 저장되며, STT 변환은 면접 종료 후 일괄 처리한다.
     *
     * @param userId     로그인 사용자 ID. TODO: JWT 인증 도입 후 인증 정보에서 추출하도록 교체
     * @param sessionId  면접 세션 ID
     * @param questionId 답변할 질문 ID
     * @param audioFile  답변 음성 파일 (LINEAR16 / 16000Hz / 모노, 최대 10MB)
     */
    @PostMapping
    public ResponseEntity<AnswerSubmitResponse> submit(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long sessionId,
            @RequestParam Long questionId,
            @RequestPart MultipartFile audioFile) {

        return ResponseEntity.ok(answerService.submit(userId, sessionId, questionId, audioFile));
    }
}