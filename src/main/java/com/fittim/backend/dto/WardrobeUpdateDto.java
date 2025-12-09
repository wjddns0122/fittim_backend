package com.fittim.backend.dto;

import com.fittim.backend.entity.Category;
import com.fittim.backend.entity.Season;

public record WardrobeUpdateDto(
        String name,
        String brand,
        String colors,
        Category category,
        Season season) {
}
