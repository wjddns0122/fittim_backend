package com.fittim.backend.service;

import com.fittim.backend.dto.FitRequestDto;
import com.fittim.backend.dto.FitResponseDto;
import com.fittim.backend.entity.Category;
import com.fittim.backend.entity.Season;
import com.fittim.backend.entity.User;
import com.fittim.backend.entity.WardrobeItem;
import com.fittim.backend.repository.UserRepository;
import com.fittim.backend.repository.WardrobeItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FitService {

    private final WardrobeItemRepository wardrobeItemRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    @Transactional(readOnly = true)
    public FitResponseDto recommend(String email, FitRequestDto request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Season season;
        try {
            season = Season.valueOf(request.season().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid season: " + request.season());
        }

        List<WardrobeItem> items = wardrobeItemRepository.findAllByUserAndSeason(user, season);

        List<WardrobeItem> tops = filterByCategory(items, Category.TOP);
        List<WardrobeItem> bottoms = filterByCategory(items, Category.BOTTOM);
        List<WardrobeItem> outers = filterByCategory(items, Category.OUTER);

        if (tops.isEmpty() || bottoms.isEmpty()) {
            throw new IllegalArgumentException("옷장에 상의와 하의가 최소 1벌씩은 있어야 추천할 수 있어요!");
        }

        WardrobeItem randomTop = getRandomItem(tops);
        WardrobeItem randomBottom = getRandomItem(bottoms);
        WardrobeItem randomOuter = null;

        if (season == Season.WINTER || season == Season.FALL) {
            if (!outers.isEmpty()) {
                randomOuter = getRandomItem(outers);
            }
        }

        return FitResponseDto.of(randomTop, randomBottom, randomOuter);
    }

    private List<WardrobeItem> filterByCategory(List<WardrobeItem> items, Category category) {
        return items.stream()
                .filter(item -> item.getCategory() == category)
                .collect(Collectors.toList());
    }

    private WardrobeItem getRandomItem(List<WardrobeItem> items) {
        if (items.isEmpty())
            return null;
        return items.get(random.nextInt(items.size()));
    }
}
