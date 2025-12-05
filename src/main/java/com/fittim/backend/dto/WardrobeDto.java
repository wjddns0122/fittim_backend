package com.fittim.backend.dto;

import com.fittim.backend.entity.Category;
import com.fittim.backend.entity.Season;
import com.fittim.backend.entity.WardrobeItem;
import java.time.LocalDateTime;

public record WardrobeDto(
        Long id,
        Category category,
        Season season,
        String imageUrl,
        LocalDateTime createdAt) {
    public static WardrobeDto from(WardrobeItem item) {
        return new WardrobeDto(
                item.getId(),
                item.getCategory(),
                item.getSeason(),
                item.getImageUrl(),
                item.getCreatedAt());
    }
}
