package com.wvw.mmw.storage;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

// GCS 연동 확인용 컨트롤러. 실제 기능 구현 시 제거 예정.
@RestController
public class GcsTestController {

    private final GcsClient gcsClient;

    public GcsTestController(GcsClient gcsClient) {
        this.gcsClient = gcsClient;
    }

    // 텍스트 파일을 업로드하고 서명 URL을 발급.
    @GetMapping(value = "/gcs/test", produces = "application/json; charset=UTF-8")
    public Map<String, String> test() {
        String objectName = "test/hello.txt";
        String content = "GCS 업로드 테스트";

        String gcsUri = gcsClient.upload(objectName, content.getBytes(), "text/plain");
        String extracted = gcsClient.extractObjectName(gcsUri);
        String signedUrl = gcsClient.generateSignedUrl(extracted, 10);

        return Map.of(
                "gcsUri", gcsUri,
                "extractedObjectName", extracted,
                "signedUrl", signedUrl
        );
    }
}