package com.fittim.backend.controller;

import com.fittim.backend.dto.WardrobeDto;
import com.fittim.backend.entity.Category;
import com.fittim.backend.entity.Season;
import com.fittim.backend.service.WardrobeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/wardrobe")
@RequiredArgsConstructor
public class WardrobeController {

    private final WardrobeService wardrobeService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<WardrobeDto> uploadItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute com.fittim.backend.dto.WardrobeItemRequest request) throws IOException {
        WardrobeDto savedItem = wardrobeService.uploadItem(
                userDetails.getUsername(),
                request);
        return ResponseEntity.ok(savedItem);
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public ResponseEntity<WardrobeDto> updateItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestBody com.fittim.backend.dto.WardrobeUpdateDto dto) {
        WardrobeDto updatedItem = wardrobeService.updateItem(id, dto, userDetails.getUsername());
        return ResponseEntity.ok(updatedItem);
    }

    @org.springframework.web.bind.annotation.PatchMapping("/{id}")
    public ResponseEntity<WardrobeDto> patchItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestBody com.fittim.backend.dto.WardrobeUpdateDto dto) {
        WardrobeDto updatedItem = wardrobeService.patchItem(id, dto, userDetails.getUsername());
        return ResponseEntity.ok(updatedItem);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        wardrobeService.deleteItem(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<WardrobeDto>> getMyWardrobe(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String season) {

        Category catEnum = null;
        if (category != null && !category.equalsIgnoreCase("ALL")) {
            try {
                catEnum = Category.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid category or handle error? For simple filtering, ignore or
                // treat as null
            }
        }

        Season seaEnum = null;
        if (season != null && !season.equalsIgnoreCase("ALL")) {
            try {
                seaEnum = Season.valueOf(season.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore
            }
        }

        List<WardrobeDto> items = wardrobeService.getMyWardrobe(userDetails.getUsername(), catEnum, seaEnum);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<WardrobeDto>> getRecentItems(@AuthenticationPrincipal UserDetails userDetails) {
        List<WardrobeDto> items = wardrobeService.getRecentItems(userDetails.getUsername());
        return ResponseEntity.ok(items);
    }
}
