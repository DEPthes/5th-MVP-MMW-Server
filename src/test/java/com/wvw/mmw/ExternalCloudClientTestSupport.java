package com.wvw.mmw;

import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.storage.Storage;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public abstract class ExternalCloudClientTestSupport {

    @MockitoBean
    private SpeechClient speechClient;

    @MockitoBean
    private TextToSpeechClient textToSpeechClient;

    @MockitoBean
    private Storage storage;
}
