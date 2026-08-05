package com.wvw.mmw.domain.interview.dto;

import com.wvw.mmw.domain.interview.entity.InterviewQuestion;
import com.wvw.mmw.domain.interview.entity.InterviewSession;
import com.wvw.mmw.domain.interview.entity.SessionStatus;
import java.util.List;

// 세션 생성 결과. 생성된 세션과 발급된 질문 목록을 담는다.
public record InterviewSessionStartResponse(
        Long sessionId,
        SessionStatus status,
        int durationMinutes,
        List<QuestionItem> questions
) {

    // questions 배열의 원소. 질문 하나의 ID·순서·내용을 담는다.
    public record QuestionItem(Long questionId, int sequence, String content) {

        static QuestionItem from(InterviewQuestion question) {
            return new QuestionItem(
                    question.getId(),
                    question.getSequence(),
                    question.getContent()
            );
        }
    }

    // 엔티티를 응답 형태로 변환한다.
    public static InterviewSessionStartResponse of(InterviewSession session,
                                                   List<InterviewQuestion> questions) {
        return new InterviewSessionStartResponse(
                session.getId(),
                session.getStatus(),
                session.getDurationMinutes(),
                questions.stream()
                        .map(QuestionItem::from)
                        .toList()
        );
    }
}