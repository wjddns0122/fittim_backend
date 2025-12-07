package com.fittim.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fittim.backend.dto.WeatherDto.SimpleWeatherDto;
import com.fittim.backend.util.KmaCoordinateConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.awt.Point;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service to fetch real-time weather data from KMA (Korea Meteorological
 * Administration).
 * converting standard Lat/Lon to KMA Grid coordinates.
 * Updates every hour at 40 minutes past the hour.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    @Value("${weather.api.key}")
    private String weatherApiKey;

    private static final String KMA_API_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst";

    private final ObjectMapper objectMapper;
    private final WebClient webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
            .build();

    /**
     * Retrieves current weather for the given coordinates.
     *
     * @param lat Latitude
     * @param lon Longitude
     * @return SimpleWeatherDto containing temperature and description
     */
    public SimpleWeatherDto getCurrentWeather(double lat, double lon) {
        log.info("Fetching KMA weather for lat: {}, lon: {}", lat, lon);

        try {
            // 1. Convert Coordinates
            Point grid = KmaCoordinateConverter.convert(lat, lon);
            log.debug("Converted to KMA Grid: ({}, {})", grid.x, grid.y);

            // 2. Calculate Base Time (API provides data 40 mins after every hour)
            LocalDateTime now = LocalDateTime.now();
            if (now.getMinute() < 45) {
                now = now.minusHours(1);
            }
            String baseDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String baseTime = now.format(DateTimeFormatter.ofPattern("HH")) + "00";

            log.debug("Requesting KMA API - Date: {}, Time: {}", baseDate, baseTime);

            // 3. Build URI with encoded ServiceKey
            String encodedKey = URLEncoder.encode(weatherApiKey, StandardCharsets.UTF_8);

            String uriString = String.format(
                    "%s?serviceKey=%s&pageNo=1&numOfRows=10&dataType=JSON&base_date=%s&base_time=%s&nx=%d&ny=%d",
                    KMA_API_URL, encodedKey, baseDate, baseTime, grid.x, grid.y);

            log.debug("Calling KMA API URL: {}", uriString);

            URI uri = URI.create(uriString);

            // 4. Call API
            String responseBody = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Trace level for full response body to avoid cluttering info logs
            log.trace("KMA Raw Response: {}", responseBody);

            // 5. Parse Response
            return parseKmaResponse(responseBody);

        } catch (Exception e) {
            log.error("Failed to fetch KMA weather: {}", e.getMessage());
        }

        // Fallback default
        return new SimpleWeatherDto("Clear", "Sunny", 20.0, "");
    }

    private SimpleWeatherDto parseKmaResponse(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode items = root.path("response").path("body").path("items").path("item");

            if (items.isMissingNode() || items.isEmpty()) {
                log.warn("KMA API returned empty items - Content might be invalid or XML error.");
                return new SimpleWeatherDto("Clear", "Sunny (No Data)", 20.0, "");
            }

            double temperature = 0.0;
            String pty = "0"; // 0:None, 1:Rain, 2:Rain/Snow, 3:Snow, 5:RainDrop, 6:RainDrop/SnowDrift,
                              // 7:SnowDrift

            for (JsonNode item : items) {
                String category = item.path("category").asText();
                String value = item.path("obsrValue").asText();

                if ("T1H".equals(category)) {
                    temperature = Double.parseDouble(value);
                } else if ("PTY".equals(category)) {
                    pty = value;
                }
            }

            String description;
            String state;
            // No Icon URL for KMA yet

            switch (pty) {
                case "1" -> {
                    description = "비";
                    state = "Rain";
                }
                case "2" -> {
                    description = "비/눈";
                    state = "Rain/Snow";
                }
                case "3" -> {
                    description = "눈";
                    state = "Snow";
                }
                case "5" -> {
                    description = "빗방울";
                    state = "Rain";
                }
                case "6" -> {
                    description = "빗방울/눈날림";
                    state = "Rain/Snow";
                }
                case "7" -> {
                    description = "눈날림";
                    state = "Snow";
                }
                default -> {
                    description = "맑음";
                    state = "Clear";
                }
            }

            return new SimpleWeatherDto(state, description, temperature, "");

        } catch (Exception e) {
            log.error("KMA Response Parsing Error: {}", e.getMessage());
            return new SimpleWeatherDto("Clear", "Sunny (Parse Error)", 20.0, "");
        }
    }
}
