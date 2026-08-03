package com.wvw.mmw.stt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * STT 연결 및 음성 지표 산출 확인용 컨트롤러.
 * 실제 기능 구현 시 제거 예정.
 */
@RestController
public class SttTestController {

    private final SttClient sttClient;
    private final VoiceMetricsCalculator calculator;
    private final String bucketName;

    public SttTestController(
            SttClient sttClient,
            VoiceMetricsCalculator calculator,
            @Value("${gcs.bucket-name}") String bucketName
    ) {
        this.sttClient = sttClient;
        this.calculator = calculator;
        this.bucketName = bucketName;
    }

    /**
     * GCS에 있는 음성 파일을 변환하고 지표를 산출한다.
     * 예: /stt/test?fileName=filler.wav
     */
    @GetMapping(value = "/stt/test", produces = "application/json; charset=UTF-8")
    public Map<String, Object> test(@RequestParam String fileName) {
        String gcsUri = "gs://" + bucketName + "/" + fileName;

        SttResult result = sttClient.transcribe(gcsUri);
        VoiceMetrics metrics = calculator.calculate(result);

        return Map.of(
                "transcript", result.getTranscript(),
                "wordCount", metrics.getWordCount(),
                "speechDurationMs", metrics.getSpeechDurationMs(),
                "silenceTotalMs", metrics.getSilenceTotalMs(),
                "longestSilenceMs", metrics.getLongestSilenceMs(),
                "rawWordArraySize", result.getWords().size()
        );
    }
}