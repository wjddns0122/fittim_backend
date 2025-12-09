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
        public WardrobeDto uploadItem(String username, MultipartFile image, Category category, Season season,
                        String name, String brand, String colors) throws IOException {
                User user = userRepository.findByEmail(username)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                if (image.isEmpty()) {
                        throw new IllegalArgumentException("Image file is empty");
                }

                // 1. Save Image
                String imageUrl = saveImage(image);

                // 2. Save Item
                WardrobeItem item = WardrobeItem.builder()
                                .user(user)
                                .category(category)
                                .season(season)
                                .imageUrl(imageUrl)
                                .name(name)
                                .brand(brand)
                                .colors(colors)
                                .build();

                WardrobeItem savedItem = wardrobeItemRepository.save(item);

                return WardrobeDto.from(savedItem);
        }

        @Transactional
        public WardrobeDto updateItem(Long id, com.fittim.backend.dto.WardrobeUpdateDto dto, String username) {
                WardrobeItem item = wardrobeItemRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

                if (!item.getUser().getEmail().equals(username)) {
                        throw new IllegalArgumentException("Unauthorized");
                }

                item.update(dto.name(), dto.brand(), dto.colors(), dto.category(), dto.season());
                return WardrobeDto.from(item);
        }

        @Transactional
        public WardrobeDto patchItem(Long id, com.fittim.backend.dto.WardrobeUpdateDto dto, String username) {
                WardrobeItem item = wardrobeItemRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

                if (!item.getUser().getEmail().equals(username)) {
                        throw new IllegalArgumentException("Unauthorized");
                }

                item.patch(dto.name(), dto.brand(), dto.colors(), dto.category(), dto.season());
                return WardrobeDto.from(item);
        }

        @Transactional
        public void deleteItem(Long id, String username) {
                WardrobeItem item = wardrobeItemRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

                if (!item.getUser().getEmail().equals(username)) {
                        throw new IllegalArgumentException("Unauthorized");
                }

                wardrobeItemRepository.delete(item);
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

        private String saveImage(MultipartFile image) throws IOException {
                String filename = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                String uploadDir = "uploads/";
                File directory = new File(uploadDir);
                if (!directory.exists()) {
                        directory.mkdir();
                }
                String filePath = uploadDir + filename;
                image.transferTo(new File(new File("").getAbsolutePath() + "/" + filePath));

                return ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/images/")
                                .path(filename)
                                .toUriString();
        }
}
