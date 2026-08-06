package com.wvw.mmw.stt;

import com.google.cloud.speech.v1.*;
import com.google.protobuf.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * STT 호출.
 * GCS에 업로드된 음성 파일을 비동기 방식으로 처리.
 */
@Component
public class SttClient {

    private final SpeechClient speechClient;
    private final String languageCode;
    private final String model;
    private final int sampleRateHertz;

    public SttClient(
            SpeechClient speechClient,
            @Value("${stt.language-code}") String languageCode,
            @Value("${stt.model}") String model,
            @Value("${stt.sample-rate-hertz}") int sampleRateHertz
    ) {
        this.speechClient = speechClient;
        this.languageCode = languageCode;
        this.model = model;
        this.sampleRateHertz = sampleRateHertz;
    }

    /**
     * GCS URI의 음성 파일을 텍스트로 변환한다.
     *
     * @param gcsUri 예: gs://버킷명/파일명.wav
     * @return 변환 텍스트와 단어별 타임스탬프
     */
    public SttResult transcribe(String gcsUri) {
        RecognitionConfig config = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setSampleRateHertz(sampleRateHertz)
                .setLanguageCode(languageCode)
                .setModel(model)                    // latest_long — 인식률이 높음
                .setEnableWordTimeOffsets(true)     // 음성 지표 산출에 필수
                .build();

        RecognitionAudio audio = RecognitionAudio.newBuilder()
                .setUri(gcsUri)
                .build();

        try {
            // 비동기 요청 후 완료까지 대기. 60초 제한이 없음.
            LongRunningRecognizeResponse response =
                    speechClient.longRunningRecognizeAsync(config, audio).get();

            return toResult(response);

        } catch (Exception e) {
            throw new IllegalStateException("STT 변환에 실패했습니다: " + gcsUri, e);
        }
    }

    /**
     * 응답에서 전체 텍스트와 단어 목록을 추출.
     * results는 침묵을 기준으로 나뉘므로, 모든 result의 단어를 하나로 합친다.
     */
    private SttResult toResult(LongRunningRecognizeResponse response) {
        StringBuilder transcript = new StringBuilder();
        List<SttWord> words = new ArrayList<>();

        for (SpeechRecognitionResult result : response.getResultsList()) {
            if (result.getAlternativesCount() == 0) {
                continue;
            }
            SpeechRecognitionAlternative alternative = result.getAlternatives(0);
            transcript.append(alternative.getTranscript());

            for (WordInfo wordInfo : alternative.getWordsList()) {
                words.add(new SttWord(
                        wordInfo.getWord(),
                        toMillis(wordInfo.getStartTime()),
                        toMillis(wordInfo.getEndTime())
                ));
            }
        }

        if (words.isEmpty()) {
            throw new IllegalStateException("STT 결과에 인식된 단어가 없습니다.");
        }

        return new SttResult(transcript.toString().trim(), words);
    }

    // protobuf Duration을 밀리초로 변환.
    private long toMillis(Duration duration) {
        return duration.getSeconds() * 1000 + duration.getNanos() / 1_000_000;
    }
}