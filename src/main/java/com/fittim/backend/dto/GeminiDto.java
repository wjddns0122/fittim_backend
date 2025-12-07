package com.fittim.backend.dto;

import lombok.Builder;

import java.util.List;

public class GeminiDto {

    @Builder
    public record GenerateContentRequest(List<Content> contents) {
    }

    @Builder
    public record Content(List<Part> parts) {
    }

    @Builder
    public record Part(String text) {
    }

    // Response structure
    public record GenerateContentResponse(List<Candidate> candidates) {
    }

    public record Candidate(Content content) {
    }

    // Internal recommendation result structure
    public record RecommendationResult(Long topId, Long bottomId, Long outerId, Long shoesId, String reason) {
    }
}
