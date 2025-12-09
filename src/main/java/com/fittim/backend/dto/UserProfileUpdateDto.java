package com.fittim.backend.dto;

import com.fittim.backend.entity.Gender;

public record UserProfileUpdateDto(
        Double height,
        Double weight,
        String bodyType,
        Gender gender,
        java.util.List<String> preferredStyles,
        java.util.List<String> preferredMalls) {
}
