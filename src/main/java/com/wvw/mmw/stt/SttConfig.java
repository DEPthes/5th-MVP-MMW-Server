package com.wvw.mmw.stt;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechSettings;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.api.gax.core.FixedCredentialsProvider;
import java.io.FileInputStream;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Google Cloud 클라이언트를 스프링 빈으로 등록.
 *
 * <p>서비스 계정 키 파일로 인증함. 키 경로는 application.yaml의
 * gcp.credentials-path로 주입받으며, 키 파일은 Git에 포함하지 않습니다.
 */
@Configuration
public class SttConfig {

    @Value("${gcp.credentials-path}")
    private String credentialsPath;

    @Value("${gcp.project-id}")
    private String projectId;

    // 서비스 계정 키 파일에서 인증 정보를 읽는다.
    @Bean
    public GoogleCredentials googleCredentials() throws IOException {
        try (FileInputStream keyFile = new FileInputStream(credentialsPath)) {
            return GoogleCredentials.fromStream(keyFile);
        }
    }

    /**
     * SpeechClient는 생성 비용이 커서 앱 전체에서 하나만 사용.
     * destroyMethod로 앱 종료 시 내부 gRPC 연결을 정리.
     */
    @Bean(destroyMethod = "close")
    public SpeechClient speechClient(GoogleCredentials credentials) throws IOException {
        SpeechSettings settings = SpeechSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();
        return SpeechClient.create(settings);
    }

    // GCS 업로드·삭제·서명 URL 발급에 사용.
    @Bean
    public Storage storage(GoogleCredentials credentials) {
        return StorageOptions.newBuilder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build()
                .getService();
    }
}