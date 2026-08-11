package com.wvw.mmw.domain.gemini.dto;

import java.util.List;

public class GeminiRequest {

    private final List<Content> contents;

    public GeminiRequest(List<Content> contents) {
        this.contents = contents;
    }

//조회 메서드
    public List<Content> getContents() {
        return contents;
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
//JSON의 text 키가 되는 부분
        public String getText() {
            return text;
        }
    }

    public static GeminiRequest of(String prompt) {
        return new GeminiRequest(
                List.of(new Content(List.of(new Part(prompt))))
        );
    }
}