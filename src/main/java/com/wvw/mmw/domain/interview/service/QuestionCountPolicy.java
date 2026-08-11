package com.wvw.mmw.domain.interview.service;

import com.wvw.mmw.global.exception.BusinessException;
import com.wvw.mmw.global.exception.ErrorCode;

/**
 * 면접 시간에 따른 질문 개수를 정한다.
 *
 * <p>질문 1턴은 TTS 재생 15초 + 답변 90초, 총 105초를 기준으로 산정한다.
 */
public class QuestionCountPolicy {

    private QuestionCountPolicy() {
    }

    /**
     * 생성할 질문 개수.
     * 시간이 남을 경우를 대비해 최대값으로 만들며, 미출제로 남는 질문이 있는 것이 정상이다.
     */
    public static int maxCount(int durationMinutes) {
        return switch (durationMinutes) {
            case 5 -> 3;
            case 10 -> 5;
            case 15 -> 7;
            case 20 -> 9;
            default -> throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        };
    }

    /**
     * 시간이 부족해도 반드시 진행해야 하는 최소 질문 개수.
     * 이 개수에 도달하기 전에는 설정 시간을 초과하더라도 면접을 계속한다.
     */
    public static int minCount(int durationMinutes) {
        return maxCount(durationMinutes) - 1;
    }
}