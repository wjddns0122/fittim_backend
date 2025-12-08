package com.fittim.backend.dto;

import com.fittim.backend.entity.WardrobeItem;

public record FitResponseDto(
        String place,
        String mood,
        String season,
        WardrobeDto top,
        WardrobeDto bottom,
        WardrobeDto outer,
        String reason) {
    public static FitResponseDto of(String place, String mood, String season, WardrobeItem top, WardrobeItem bottom,
            WardrobeItem outer, String reason) {
        return new FitResponseDto(
                place,
                mood,
                season,
                top != null ? WardrobeDto.from(top) : null,
                bottom != null ? WardrobeDto.from(bottom) : null,
                outer != null ? WardrobeDto.from(outer) : null,
                reason);
    }
}
