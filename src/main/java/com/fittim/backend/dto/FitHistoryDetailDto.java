package com.fittim.backend.dto;

import com.fittim.backend.entity.FitHistory;
import java.time.format.DateTimeFormatter;

public record FitHistoryDetailDto(
        Long id,
        String title,
        String place,
        String mood,
        String season,
        String reason,
        String createdAt,
        WardrobeDto top,
        WardrobeDto bottom,
        WardrobeDto outer) {

    public static FitHistoryDetailDto from(FitHistory entity) {
        String displayTitle = "오늘의 " + (entity.getPlace() != null ? entity.getPlace() : "데일리") + " 룩";

        return new FitHistoryDetailDto(
                entity.getId(),
                displayTitle,
                entity.getPlace(),
                entity.getMood(),
                entity.getSeason(),
                entity.getRecommendationReason(),
                entity.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME),
                WardrobeDto.from(entity.getTop()),
                WardrobeDto.from(entity.getBottom()),
                entity.getOuter() != null ? WardrobeDto.from(entity.getOuter()) : null);
    }
}
