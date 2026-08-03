package com.wvw.mmw.stt;

/**
 * STT가 인식한 단어 하나와 발화 구간.
 * latest_long 모델은 음절 단위로 반환하며,
 * 어절 시작에는 구분자(U+2581)가 붙음(언더바(_)와 혼동 주의).
 */
public class SttWord {

    private final String word;
    private final long startTimeMs;
    private final long endTimeMs;

    public SttWord(String word, long startTimeMs, long endTimeMs) {
        this.word = word;
        this.startTimeMs = startTimeMs;
        this.endTimeMs = endTimeMs;
    }

    public String getWord() {
        return word;
    }

    public long getStartTimeMs() {
        return startTimeMs;
    }

    public long getEndTimeMs() {
        return endTimeMs;
    }
}