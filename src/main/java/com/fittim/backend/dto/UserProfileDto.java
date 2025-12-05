package com.fittim.backend.dto;

import com.fittim.backend.entity.User;

public record UserProfileDto(
        Long id,
        String email,
        String nickname,
        String role) {
    public static UserProfileDto from(User user) {
        return new UserProfileDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole());
    }
}
