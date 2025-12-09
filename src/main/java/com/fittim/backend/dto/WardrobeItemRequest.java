package com.fittim.backend.dto;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public record WardrobeItemRequest(
        MultipartFile image,
        String category,
        String season,
        String name,
        String brand,
        List<String> seasons,
        List<String> colors) {
}
