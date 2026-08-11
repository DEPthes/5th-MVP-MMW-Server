package com.wvw.mmw.gemini.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Gemini API 응답 구조를 표현하는 DTO.
 * 실제 응답에는 usageMetadata, modelVersion 등 다른 필드도 포함되지만,
 * 사용하지 않는 값은 무시한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiResponse {

    //Gemini 답변 저장소
    private final List<Candidate> candidates;

    //JSON에서 candidates키를 찾아서 넣는 역할
    public GeminiResponse(@JsonProperty("candidates") List<Candidate> candidates) {
        this.candidates = candidates;
    }

    //조회 메서드
    public List<Candidate> getCandidates() {
        return candidates;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Candidate {
        private final Content content;

        public Candidate(@JsonProperty("content") Content content) {
            this.content = content;
        }

        public Content getContent() {
            return content;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {
        private final List<Part> parts;

        public Content(@JsonProperty("parts") List<Part> parts) {
            this.parts = parts;
        }

        public List<Part> getParts() {
            return parts;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Part {
        private final String text;

        public Part(@JsonProperty("text") String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    //예외처리
    //candidates == null: 응답에 해당 키가 아예 없는 경우
    //candidates.isEmpty(): 해당 키가 있는데 배열이 비어있는 경우
    //Gemini 필터로 인해 답변을 거부하면 생기는 오류
    public String firstText() {
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalStateException("Gemini 응답에 candidates가 없습니다.");
        }

        //get(0) -> 첫 번째 답변 반환
        Content content = candidates.get(0).getContent();
        if (content == null || content.getParts() == null || content.getParts().isEmpty()) {
            throw new IllegalStateException("Gemini 응답에 parts가 없습니다.");
        }

        return content.getParts().get(0).getText();
    }
}