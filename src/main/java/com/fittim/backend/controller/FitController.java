package com.fittim.backend.controller;

import com.fittim.backend.dto.FitRequestDto;
import com.fittim.backend.dto.FitResponseDto;
import com.fittim.backend.service.FitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fits")
@RequiredArgsConstructor
public class FitController {

    private final FitService fitService;

    @PostMapping("/recommend")
    public ResponseEntity<FitResponseDto> recommend(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody FitRequestDto request) {
        FitResponseDto recommendation = fitService.recommend(userDetails.getUsername(), request);
        return ResponseEntity.ok(recommendation);
    }

    @org.springframework.web.bind.annotation.GetMapping("/history")
    public ResponseEntity<java.util.List<com.fittim.backend.dto.FitHistoryDto>> getFitHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(fitService.getFitHistory(userDetails.getUsername()));
    }
}
