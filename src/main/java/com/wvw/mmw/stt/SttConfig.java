package com.wvw.mmw.stt;

import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Google Cloud 클라이언트를 스프링 빈으로 등록.
 * 인증은 ADC로 자동 처리되므로
 * 코드에 키나 프로젝트 정보는 넣지 않았습니다.
 */
@Configuration
public class SttConfig {

    /**
     * SpeechClient는 생성 비용이 커서 앱 전체에서 하나만 사용.
     * destroyMethod로 앱 종료 시 내부 gRPC 연결을 정리.
     * gRPC가 자바 예전 기능을 사용해 경고가 뜨는데 무시하셔도 됩니다.
     */
    @Bean(destroyMethod = "close")
    public SpeechClient speechClient() throws IOException {
        return SpeechClient.create();
    }

    // GCS 업로드·삭제에 사용.
    @Bean
    public Storage storage() {
        return StorageOptions.getDefaultInstance().getService();
    }
}