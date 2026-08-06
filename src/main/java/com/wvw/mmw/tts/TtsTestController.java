package com.wvw.mmw.tts;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

/** TTS 연결 확인용 컨트롤러. 실제 기능 구현 시 제거 예정. */
@RestController
public class TtsTestController {

    private final TtsClient ttsClient;

    public TtsTestController(TtsClient ttsClient) {
        this.ttsClient = ttsClient;
    }

    /**
     * 텍스트를 음성으로 변환해 MP3로 반환한다.
     * 브라우저에서 접속하면 바로 재생된다.
     */
    @GetMapping(value = "/tts/test", produces = "audio/mpeg")
    public ResponseEntity<byte[]> test(
            @RequestParam(defaultValue = "안녕하세요, 모의 면접을 시작하겠습니다.") String text) {

        byte[] audio = ttsClient.synthesize(text);

        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=\"question.mp3\"")
                .body(audio);
    }
}