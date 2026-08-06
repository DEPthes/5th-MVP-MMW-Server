package com.wvw.mmw.tts;

import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Google Cloud TTS를 호출해 텍스트를 음성으로 변환.
 * 면접 질문을 음성으로 읽어주는 데 사용.
 */
@Component
public class TtsClient {

    private final TextToSpeechClient textToSpeechClient;
    private final String languageCode;
    private final SsmlVoiceGender voiceGender;
    private final double speakingRate;

    public TtsClient(
            TextToSpeechClient textToSpeechClient,
            @Value("${tts.language-code}") String languageCode,
            @Value("${tts.voice-gender}") String voiceGender,
            @Value("${tts.speaking-rate}") double speakingRate
    ) {
        this.textToSpeechClient = textToSpeechClient;
        this.languageCode = languageCode;
        this.voiceGender = SsmlVoiceGender.valueOf(voiceGender);
        this.speakingRate = speakingRate;
    }

    /**
     * 텍스트를 MP3 음성 데이터로 변환.
     *
     * @param text 변환할 텍스트. 요청당 5,000바이트 제한이 있음
     * @return MP3 바이트 배열. GCS에 업로드하거나 직접 반환할 수 있음
     */
    public byte[] synthesize(String text) {
        SynthesisInput input = SynthesisInput.newBuilder()
                .setText(text)
                .build();

        VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode(languageCode)
                .setSsmlGender(voiceGender)
                .build();

        AudioConfig audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.MP3)
                .setSpeakingRate(speakingRate)
                .build();

        SynthesizeSpeechResponse response =
                textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

        ByteString audioContent = response.getAudioContent();
        if (audioContent.isEmpty()) {
            throw new IllegalStateException("TTS 응답에 음성 데이터가 없습니다.");
        }

        return audioContent.toByteArray();
    }
}