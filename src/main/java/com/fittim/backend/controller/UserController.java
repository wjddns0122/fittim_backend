package com.fittim.backend.controller;

import com.fittim.backend.dto.UserProfileDto;
import com.fittim.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        UserProfileDto profile = userService.getMyProfile(userDetails.getUsername());
        return ResponseEntity.ok(profile);
    }

    @org.springframework.web.bind.annotation.PutMapping("/me")
    public ResponseEntity<UserProfileDto> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @org.springframework.web.bind.annotation.RequestBody com.fittim.backend.dto.UserProfileUpdateDto dto) {
        UserProfileDto updatedProfile = userService.updateProfile(userDetails.getUsername(), dto);
        return ResponseEntity.ok(updatedProfile);
    }

    @org.springframework.web.bind.annotation.PatchMapping("/me")
    public ResponseEntity<UserProfileDto> patchProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @org.springframework.web.bind.annotation.RequestBody com.fittim.backend.dto.UserProfileUpdateDto dto) {
        UserProfileDto updatedProfile = userService.patchProfile(userDetails.getUsername(), dto);
        return ResponseEntity.ok(updatedProfile);
    }
}
