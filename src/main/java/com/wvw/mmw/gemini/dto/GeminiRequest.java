package com.wvw.mmw.gemini.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeminiRequest {

    private final List<Content> contents; //무엇을 물어볼지
    private final GenerationConfig generationConfig; //어떻게 답해달라

    public GeminiRequest(List<Content> contents, GenerationConfig generationConfig) {
        this.contents = contents;
        this.generationConfig = generationConfig;
    }

    public List<Content> getContents() {
        return contents;
    }

    public GenerationConfig getGenerationConfig() {
        return generationConfig;
    }

    public static class Content {
        private final List<Part> parts;

        public Content(List<Part> parts) {
            this.parts = parts;
        }

        public List<Part> getParts() {
            return parts;
        }
    }

    public static class Part {
        private final String text;

        public Part(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    /**
     * 응답 형식을 지정.
     * responseSchema를 주면 Gemini가 해당 구조의 JSON만 반환함.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GenerationConfig {
        private final String responseMimeType; //응답 형식; Gemini가 순수 JSON만 반환함
        private final Map<String, Object> responseSchema;

        public GenerationConfig(String responseMimeType, Map<String, Object> responseSchema) {
            this.responseMimeType = responseMimeType;
            this.responseSchema = responseSchema;
        }

        public String getResponseMimeType() {
            return responseMimeType;
        }

        public Map<String, Object> getResponseSchema() {
            return responseSchema;
        }
    }

    // 일반 텍스트 응답을 받는 요청.
    public static GeminiRequest of(String prompt) {
        return new GeminiRequest(
                List.of(new Content(List.of(new Part(prompt)))),
                null
        );
    }

    // 지정한 스키마의 JSON 응답을 받는 요청.
    public static GeminiRequest ofJson(String prompt, Map<String, Object> responseSchema) {
        return new GeminiRequest(
                List.of(new Content(List.of(new Part(prompt)))),
                new GenerationConfig("application/json", responseSchema)
        );
    }
}