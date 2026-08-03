package com.wvw.mmw.stt;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * STT 결과에서 음성 지표를 산출.
 * 산출한 값은 종합 피드백 프롬프트에 전달되며 화면에는 노출하지 않음.
 */
@Component
public class VoiceMetricsCalculator {

    // 어절 구분자(U+2581). latest_long 모델이 어절 시작 토큰 앞에 붙인다.
    private static final String WORD_SEPARATOR = "\u2581";

    // 이 시간 이상 벌어진 단어 간격만 침묵으로 봄. 자연스러운 호흡은 제외.
    private static final long SILENCE_THRESHOLD_MS = 1000;

    public VoiceMetrics calculate(SttResult sttResult) {
        List<SttWord> words = sttResult.getWords();

        return new VoiceMetrics(
                countWords(words),
                calculateSpeechDuration(words),
                calculateSilenceTotal(words),
                calculateLongestSilence(words)
        );
    }

    /**
     * 어절 수를 센다.
     * latest_long 모델은 음절 단위로 반환하므로 배열 크기가 아닌 어절 구분자 기준으로 구분.
     * 예: "저는 백엔드" → [▁저는, ▁, 백, 엔, 드] (배열 5개, 어절 2개)
     */
    private int countWords(List<SttWord> words) {
        return (int) words.stream()
                .filter(w -> w.getWord().startsWith(WORD_SEPARATOR))
                .count();
    }

    // 첫 단어 시작부터 마지막 단어 끝까지, 침묵을 포함한 전체 발화 시간.
    private long calculateSpeechDuration(List<SttWord> words) {
        long start = words.get(0).getStartTimeMs();
        long end = words.get(words.size() - 1).getEndTimeMs();
        return end - start;
    }

    // 기준 이상 벌어진 단어 간격의 합.
    private long calculateSilenceTotal(List<SttWord> words) {
        long total = 0;
        for (int i = 1; i < words.size(); i++) {
            long gap = gapBetween(words.get(i - 1), words.get(i));
            if (gap >= SILENCE_THRESHOLD_MS) {
                total += gap;
            }
        }
        return total;
    }

    // 가장 긴 침묵 한 구간.
    private long calculateLongestSilence(List<SttWord> words) {
        long longest = 0;
        for (int i = 1; i < words.size(); i++) {
            long gap = gapBetween(words.get(i - 1), words.get(i));
            if (gap >= SILENCE_THRESHOLD_MS) {
                longest = Math.max(longest, gap);
            }
        }
        return longest;
    }

    private long gapBetween(SttWord previous, SttWord current) {
        return current.getStartTimeMs() - previous.getEndTimeMs();
    }
}