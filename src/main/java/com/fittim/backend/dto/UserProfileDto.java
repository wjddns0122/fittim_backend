package com.fittim.backend.dto;

import com.fittim.backend.entity.User;

public record UserProfileDto(
        Long id,
        String email,
        String nickname,
        String role,
        Double height,
        Double weight,
        String bodyType,
        com.fittim.backend.entity.Gender gender,
        java.util.List<String> preferredStyles,
        java.util.List<String> preferredMalls) {
    public static UserProfileDto from(User user) {
        return new UserProfileDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                user.getHeight(),
                user.getWeight(),
                user.getBodyType(),
                user.getGender(),
                user.getPreferredStyles(),
                user.getPreferredMalls());
    }
}
