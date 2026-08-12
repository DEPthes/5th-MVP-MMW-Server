package com.wvw.mmw.tts;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.TextToSpeechSettings;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Google Cloud TTS 클라이언트를 스프링 빈으로 등록.
 * 인증 정보는 SttConfig에서 등록한 GoogleCredentials 빈을 주입받아 공유.
 */
@Configuration
public class TtsConfig {

    // SpeechClient와 마찬가지로 생성 비용이 커서 하나만 사용함.
    @Bean(destroyMethod = "close")
    public TextToSpeechClient textToSpeechClient(GoogleCredentials credentials) throws IOException {
        TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();
        return TextToSpeechClient.create(settings);
    }
}