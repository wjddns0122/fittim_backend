package com.fittim.backend.dto;

import com.fittim.backend.entity.WardrobeItem;

public record FitResponseDto(
        WardrobeDto top,
        WardrobeDto bottom,
        WardrobeDto outer) {
    public static FitResponseDto of(WardrobeItem top, WardrobeItem bottom, WardrobeItem outer) {
        return new FitResponseDto(
                top != null ? WardrobeDto.from(top) : null,
                bottom != null ? WardrobeDto.from(bottom) : null,
                outer != null ? WardrobeDto.from(outer) : null);
    }
}
