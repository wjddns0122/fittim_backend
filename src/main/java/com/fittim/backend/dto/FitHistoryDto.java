package com.fittim.backend.dto;

import com.fittim.backend.entity.FitHistory;

import java.time.format.DateTimeFormatter;

public record FitHistoryDto(
        Long id,
        String title, // "오늘의 {place} 룩"
        String place,
        String mood,
        String imageUrl, // Representative image (Outer > Top)
        String createdAt) {
    public static FitHistoryDto from(FitHistory entity) {
        String mainImageUrl = (entity.getOuter() != null) ? entity.getOuter().getImageUrl()
                : entity.getTop().getImageUrl();
        String displayTitle = "오늘의 " + (entity.getPlace() != null ? entity.getPlace() : "데일리") + " 룩";

        return new FitHistoryDto(
                entity.getId(),
                displayTitle,
                entity.getPlace(),
                entity.getMood(),
                mainImageUrl,
                entity.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME));
    }
}
