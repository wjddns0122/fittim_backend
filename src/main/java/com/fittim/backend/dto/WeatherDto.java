package com.fittim.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class WeatherDto {

    @Getter
    @NoArgsConstructor
    public static class OpenWeatherResponse {
        private List<Weather> weather;
        private Main main;
        private String name;
    }

    @Getter
    @NoArgsConstructor
    public static class Weather {
        private String main; // "Clear", "Rain", etc.
        private String description; // "clear sky"
        private String icon;
    }

    @Getter
    @NoArgsConstructor
    public static class Main {
        private double temp;
        @JsonProperty("feels_like")
        private double feelsLike;
        private double humidity;
    }

    public record SimpleWeatherDto(
            String state,
            String description,
            double temperature,
            String iconUrl) {
    }
}
