package com.wvw.mmw.domain.interview.dto;

import com.wvw.mmw.domain.interview.entity.Answer;
import com.wvw.mmw.domain.interview.entity.AnswerStatus;

/**
 * 답변 음성 제출 결과.
 *
 * @param answerId   생성된 답변 ID
 * @param questionId 답변한 질문 ID
 * @param status     답변 상태. 업로드 직후에는 UPLOADED.
 */
public record AnswerSubmitResponse(
        Long answerId,
        Long questionId,
        AnswerStatus status
) {

    public static AnswerSubmitResponse from(Answer answer) {
        return new AnswerSubmitResponse(
                answer.getId(),
                answer.getInterviewQuestion().getId(),
                answer.getStatus()
        );
    }
}