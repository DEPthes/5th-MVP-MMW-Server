package com.wvw.mmw.stt;

/**
 * STT 응답에서 산출한 음성 지표.
 * ERD answers 테이블의 지표 컬럼과 1:1로 대응.
 */
public class VoiceMetrics {

    private final int wordCount;
    private final long speechDurationMs;
    private final long silenceTotalMs;
    private final long longestSilenceMs;

    public VoiceMetrics(int wordCount, long speechDurationMs,
                        long silenceTotalMs, long longestSilenceMs) {
        this.wordCount = wordCount;
        this.speechDurationMs = speechDurationMs;
        this.silenceTotalMs = silenceTotalMs;
        this.longestSilenceMs = longestSilenceMs;
    }

    public int getWordCount() {
        return wordCount;
    }

    public long getSpeechDurationMs() {
        return speechDurationMs;
    }

    public long getSilenceTotalMs() {
        return silenceTotalMs;
    }

    public long getLongestSilenceMs() {
        return longestSilenceMs;
    }
}