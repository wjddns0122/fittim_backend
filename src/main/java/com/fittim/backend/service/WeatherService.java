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

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    @Value("${weather.api.key}")
    private String weatherApiKey;

    private static final String KMA_API_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst";

    private final ObjectMapper objectMapper;
    // Encoding mode explicitly handled
    private final WebClient webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
            .build();

    public SimpleWeatherDto getCurrentWeather(double lat, double lon) {
        log.info("Fetching KMA weather for lat: {}, lon: {}", lat, lon);

        try {
            // 1. Convert Coordinates
            Point grid = KmaCoordinateConverter.convert(lat, lon);
            System.out.println(">>> [KMA] Request Lat/Lon: " + lat + ", " + lon);
            System.out.println(">>> [KMA] Converted X/Y: " + grid.x + ", " + grid.y);

            // 2. Calculate Base Time
            LocalDateTime now = LocalDateTime.now();
            if (now.getMinute() < 45) {
                now = now.minusHours(1);
            }
            String baseDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String baseTime = now.format(DateTimeFormatter.ofPattern("HH")) + "00";

            System.out.println(">>> [KMA] BaseTime: " + baseDate + " " + baseTime);

            // 3. Build URI manually to control encoding of the ServiceKey
            // KMA API keys often break with standard encoding if they contain special chars
            // like '+', '/', etc.
            // Assuming the injected key is the "Decoding Key", we should encode it.
            // If it interacts with UriComponentsBuilder, it might get double-encoded.
            // Safest way for KMA: Construct query string manually with encoded key.

            String encodedKey = URLEncoder.encode(weatherApiKey, StandardCharsets.UTF_8);

            String uriString = String.format(
                    "%s?serviceKey=%s&pageNo=1&numOfRows=10&dataType=JSON&base_date=%s&base_time=%s&nx=%d&ny=%d",
                    KMA_API_URL, encodedKey, baseDate, baseTime, grid.x, grid.y);

            System.out.println(">>> [KMA] Calling URL: " + uriString);

            URI uri = URI.create(uriString);

            // 4. Call API
            String responseBody = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.debug("KMA Response: {}", responseBody);

            // 5. Parse Response
            return parseKmaResponse(responseBody);

        } catch (Exception e) {
            log.error("Failed to fetch KMA weather: {}", e.getMessage());
        }

        // Fallback
        return new SimpleWeatherDto("Clear", "Sunny", 20.0, "");
    }

    private SimpleWeatherDto parseKmaResponse(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode items = root.path("response").path("body").path("items").path("item");

            if (items.isMissingNode() || items.isEmpty()) {
                log.warn("KMA API returned empty items.");
                return new SimpleWeatherDto("Clear", "Sunny (No Data)", 20.0, "");
            }

            double temperature = 0.0;
            String pty = "0"; // 강수형태 (0:없음, 1:비, 2:비/눈, 3:눈, 5:빗방울, 6:빗방울눈날림, 7:눈날림)

            for (JsonNode item : items) {
                String category = item.path("category").asText();
                String value = item.path("obsrValue").asText();

                if ("T1H".equals(category)) {
                    temperature = Double.parseDouble(value);
                } else if ("PTY".equals(category)) {
                    pty = value;
                }
            }

            String description = "맑음";
            String state = "Clear";
            String iconUrl = ""; // No icon for KMA impl yet, reusing OpenWeather generic or empty

            switch (pty) {
                case "1":
                    description = "비";
                    state = "Rain";
                    break;
                case "2":
                    description = "비/눈";
                    state = "Rain/Snow";
                    break;
                case "3":
                    description = "눈";
                    state = "Snow";
                    break;
                case "5":
                    description = "빗방울";
                    state = "Rain";
                    break;
                case "6":
                    description = "빗방울/눈날림";
                    state = "Rain/Snow";
                    break;
                case "7":
                    description = "눈날림";
                    state = "Snow";
                    break;
                case "0":
                default:
                    description = "맑음";
                    state = "Clear";
                    break;
            }

            return new SimpleWeatherDto(state, description, temperature, iconUrl);

        } catch (Exception e) {
            log.error("Parsing Error: {}", e.getMessage());
            return new SimpleWeatherDto("Clear", "Sunny (Parse Error)", 20.0, "");
        }
    }
}
