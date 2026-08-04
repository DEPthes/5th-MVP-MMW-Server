package com.wvw.mmw.tts;

import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Google Cloud TTS 클라이언트를 스프링 빈으로 등록.
 * 인증은 ADC로 자동 처리.
 */
@Configuration
public class TtsConfig {

    /** SpeechClient와 마찬가지로 생성 비용이 커서 하나만 사용한다. */
    @Bean(destroyMethod = "close")
    public TextToSpeechClient textToSpeechClient() throws IOException {
        return TextToSpeechClient.create();
    }
}