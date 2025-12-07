package com.fittim.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fittim.backend.dto.GeminiDto;
import com.fittim.backend.dto.GeminiDto.Content;
import com.fittim.backend.dto.GeminiDto.GenerateContentRequest;
import com.fittim.backend.dto.GeminiDto.Part;
import com.fittim.backend.dto.GeminiDto.RecommendationResult;
import com.fittim.backend.entity.Category;
import com.fittim.backend.entity.WardrobeItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    private final ObjectMapper objectMapper;
    private final WebClient webClient = WebClient.create();

    public RecommendationResult recommend(List<WardrobeItem> userItems, String place, String mood, String season,
            String weather) {
        try {
            String prompt = createPrompt(userItems, place, mood, season, weather);
            String responseJson = callGeminiApi(prompt);
            return parseResponse(responseJson);
        } catch (Exception e) {
            log.error("Gemini API Error: {}", e.getMessage());
            return null; // Fallback will be handled by calling service
        }
    }

    private String createPrompt(List<WardrobeItem> items, String place, String mood, String season, String weather)
            throws JsonProcessingException {
        // Simple item representation for token efficiency
        List<Map<String, Object>> simpleItems = items.stream().map(item -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", item.getId());
            map.put("category", item.getCategory().name());
            map.put("season", item.getSeason().name());
            return map;
        }).collect(Collectors.toList());

        String itemsJson = objectMapper.writeValueAsString(simpleItems);

        return String.format(
                """
                        너는 20대 MZ세대를 위한 미니멀 패션 스타일리스트야.
                        아래 [옷장 목록] 중에서 [상황: %s, %s, %s, 날씨: %s]에 가장 잘 어울리는 코디를 조합해줘.

                        [응답 규칙]
                        1. 무조건 아래 JSON 형식으로만 응답할 것 (Markdown 코드블록 금지).
                        2. `reason` 필드에 **이 코디를 추천한 구체적인 이유**를 한국어로 1~2문장 작성할 것. (예: "오늘 날씨가 맑아서 화사한 베이지 톤으로 매치했습니다.")

                        [JSON 형식]
                        {
                          "topId": (정수),
                          "bottomId": (정수),
                          "outerId": (정수 or null),
                          "shoesId": (정수 or null),
                          "reason": "여기에 추천 사유 작성"
                        }

                        [옷장 목록]
                        %s
                        """,
                place, mood, season, weather, itemsJson);
    }

    private String callGeminiApi(String prompt) {
        log.info("Calling Gemini Model: {}", geminiApiUrl);
        log.debug("Using API Key: {}...", geminiApiKey.substring(0, Math.min(10, geminiApiKey.length())));

        GenerateContentRequest request = GenerateContentRequest.builder()
                .contents(List.of(
                        Content.builder()
                                .parts(List.of(Part.builder().text(prompt).build()))
                                .build()))
                .build();

        try {
            GeminiDto.GenerateContentResponse response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("generativelanguage.googleapis.com")
                            .path("/v1beta/models/gemini-2.5-flash:generateContent")
                            .queryParam("key", geminiApiKey)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GeminiDto.GenerateContentResponse.class)
                    .block();

            if (response != null && !response.candidates().isEmpty()) {
                return response.candidates().get(0).content().parts().get(0).text();
            }
        } catch (Exception e) {
            throw new RuntimeException("Gemini Call Failed: " + e.getMessage());
        }
        throw new RuntimeException("Empty response from Gemini");
    }

    private RecommendationResult parseResponse(String rawText) {
        try {
            // Cleanup markdown code blocks if present
            String jsonText = rawText.replaceAll("```json", "").replaceAll("```", "").trim();
            return objectMapper.readValue(jsonText, RecommendationResult.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse Gemini response: {}", rawText);
            throw new RuntimeException("JSON Parsing Failed");
        }
    }
}
