package com.wvw.mmw.storage;

import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * GCS에 음성 파일을 저장하고 접근 URL을 발급.
 * 답변 음성(STT 처리 대상)과 질문 음성(TTS 결과)을 모두 다룸.
 */
@Component
public class GcsClient {

    private final Storage storage;
    private final String bucketName;

    public GcsClient(Storage storage, @Value("${gcs.bucket-name}") String bucketName) {
        this.storage = storage;
        this.bucketName = bucketName;
    }

    /**
     * 파일을 업로드하고 GCS URI를 반환.
     *
     * @param objectName 버킷 내 경로. 예: answers/15/101.wav
     * @param content    파일 내용
     * @param contentType 예: audio/wav, audio/mpeg
     * @return gs:// 로 시작하는 URI. STT 요청에 그대로 사용.
     */
    public String upload(String objectName, byte[] content, String contentType) {
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();

        storage.create(blobInfo, content);

        return "gs://" + bucketName + "/" + objectName;
    }

    /**
     * 서명된 접근 URL을 발급.
     * 만료 시간이 지나면 URL이 무효화되므로 링크가 유출돼도 영구 접근은 불가능함.
     *
     * @param objectName 버킷 내 경로
     * @param durationMinutes 유효 시간(분)
     */
    public String generateSignedUrl(String objectName, long durationMinutes) {
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, objectName)).build();

        return storage.signUrl(blobInfo, durationMinutes, TimeUnit.MINUTES,
                Storage.SignUrlOption.withV4Signature()).toString();
    }

    /**
     * 파일을 삭제.
     *
     * @return 삭제 성공 여부. 파일이 없으면 false
     */
    public boolean delete(String objectName) {
        return storage.delete(BlobId.of(bucketName, objectName));
    }

    // gs://버킷/경로 형태의 URI에서 경로 부분만 꺼낸다.
    public String extractObjectName(String gcsUri) {
        String prefix = "gs://" + bucketName + "/";
        if (!gcsUri.startsWith(prefix)) {
            throw new IllegalArgumentException("이 버킷의 URI가 아닙니다: " + gcsUri);
        }
        return gcsUri.substring(prefix.length());
    }
}