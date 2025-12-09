package com.fittim.backend.dto;

import com.fittim.backend.entity.Category;
import com.fittim.backend.entity.Season;
import com.fittim.backend.entity.WardrobeItem;
import java.time.LocalDateTime;

import java.util.List;

public record WardrobeDto(
        Long id,
        Category category,
        Season season,
        String imageUrl,
        String name,
        String brand,
        List<String> seasons,
        List<String> colors,
        LocalDateTime createdAt) {
    public static WardrobeDto from(WardrobeItem item) {
        return new WardrobeDto(
                item.getId(),
                item.getCategory(),
                item.getSeason(),
                item.getImageUrl(),
                item.getName(),
                item.getBrand(),
                item.getSeasons(),
                item.getColors(),
                item.getCreatedAt());
    }
}
