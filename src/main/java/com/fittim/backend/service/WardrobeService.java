package com.fittim.backend.service;

import com.fittim.backend.dto.WardrobeDto;
import com.fittim.backend.entity.Category;
import com.fittim.backend.entity.Season;
import com.fittim.backend.entity.User;
import com.fittim.backend.entity.WardrobeItem;
import com.fittim.backend.repository.UserRepository;
import com.fittim.backend.repository.WardrobeItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WardrobeService {

        private final WardrobeItemRepository wardrobeItemRepository;
        private final UserRepository userRepository;

        @Transactional
        public WardrobeDto uploadItem(String email, MultipartFile image, Category category, Season season)
                        throws IOException {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                // Save image to local file system
                String filename = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                String uploadDir = "uploads/";
                File directory = new File(uploadDir);
                if (!directory.exists()) {
                        directory.mkdir();
                }
                String filePath = uploadDir + filename;
                image.transferTo(new File(new File("").getAbsolutePath() + "/" + filePath));

                // Generate URL
                String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/images/")
                                .path(filename)
                                .toUriString();

                // Save metadata to DB
                WardrobeItem item = WardrobeItem.builder()
                                .user(user)
                                .category(category)
                                .season(season)
                                .imageUrl(fileUrl)
                                .build();

                wardrobeItemRepository.save(item);

                return WardrobeDto.from(item);
        }

        @Transactional(readOnly = true)
        public List<WardrobeDto> getMyWardrobe(String email, Category category, Season season) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                return wardrobeItemRepository.findByUserIdAndCategoryAndSeason(user.getId(), category, season).stream()
                                .map(WardrobeDto::from)
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public List<WardrobeDto> getRecentItems(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                return wardrobeItemRepository.findTop5ByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                                .map(WardrobeDto::from)
                                .collect(Collectors.toList());
        }
}
