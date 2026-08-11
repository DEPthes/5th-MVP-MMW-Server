package com.wvw.mmw.stt;

import java.util.List;

// STT 변환 결과. 텍스트와 단어별 타임스탬프를 담는다.
public class SttResult {

    private final String transcript;
    private final List<SttWord> words;

    public SttResult(String transcript, List<SttWord> words) {
        this.transcript = transcript;
        this.words = words;
    }

    public String getTranscript() {
        return transcript;
    }

    public List<SttWord> getWords() {
        return words;
    }
}