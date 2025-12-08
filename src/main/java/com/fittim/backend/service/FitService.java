package com.fittim.backend.service;

import com.fittim.backend.dto.FitRequestDto;
import com.fittim.backend.dto.FitResponseDto;
import com.fittim.backend.entity.Category;
import com.fittim.backend.entity.FitHistory;
import com.fittim.backend.entity.Season;
import com.fittim.backend.entity.User;
import com.fittim.backend.entity.WardrobeItem;
import com.fittim.backend.repository.FitHistoryRepository;
import com.fittim.backend.repository.UserRepository;
import com.fittim.backend.repository.WardrobeItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Core Service for AI-based Outfit Recommendation.
 * Orchestrates Wardrobe retrieval, Gemini AI calling, and History saving.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FitService {

    private final GeminiService geminiService;
    private final FitHistoryRepository fitHistoryRepository;
    private final WardrobeItemRepository wardrobeItemRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    /**
     * Recommends an outfit based on user's wardrobe and request context.
     * Tries AI first, resorts to random fallback on failure.
     *
     * @param email   User email
     * @param request Recommendation context (Place, Mood, Season, Weather)
     * @return FitResponseDto with recommended items and reason
     */
    @Transactional
    public FitResponseDto recommend(String email, FitRequestDto request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Season season;
        try {
            season = Season.valueOf(request.season().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid season: " + request.season());
        }

        List<WardrobeItem> items;
        if (season == Season.ALL) {
            items = wardrobeItemRepository.findAllByUserOrderByCreatedAtDesc(user);
        } else {
            List<WardrobeItem> seasonItems = wardrobeItemRepository.findAllByUserAndSeason(user, season);
            List<WardrobeItem> allSeasonItems = wardrobeItemRepository.findAllByUserAndSeason(user, Season.ALL);

            items = new java.util.ArrayList<>(seasonItems);
            items.addAll(allSeasonItems);
        }

        // Optimization: Limit items sent to AI to most recent 30
        List<WardrobeItem> recentItems = items.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(30)
                .collect(Collectors.toList());

        List<WardrobeItem> tops = filterByCategory(items, Category.TOP);
        List<WardrobeItem> bottoms = filterByCategory(items, Category.BOTTOM);
        List<WardrobeItem> outers = filterByCategory(items, Category.OUTER);

        if (tops.isEmpty() || bottoms.isEmpty()) {
            throw new IllegalArgumentException("옷장에 상의와 하의가 최소 1벌씩은 있어야 추천할 수 있어요!");
        }

        WardrobeItem recommendedTop = null;
        WardrobeItem recommendedBottom = null;
        WardrobeItem recommendedOuter = null;
        String recommendedReason = null;

        // 1. Try AI Recommendation
        try {
            // Use weather from request or fallback
            String weather = (request.weather() != null && !request.weather().isEmpty())
                    ? request.weather()
                    : "Sunny, 20°C"; // Default fallback

            com.fittim.backend.dto.GeminiDto.RecommendationResult aiResult = geminiService.recommend(recentItems,
                    request.place(), request.mood(), season.name(), weather);

            if (aiResult != null) {
                if (aiResult.topId() != null)
                    recommendedTop = findItemById(items, aiResult.topId());
                if (aiResult.bottomId() != null)
                    recommendedBottom = findItemById(items, aiResult.bottomId());
                if (aiResult.outerId() != null)
                    recommendedOuter = findItemById(items, aiResult.outerId());
                recommendedReason = aiResult.reason();
            }
        } catch (Exception e) {
            // Log error and fall back to random
            log.warn("AI Recommendation Failed (Falling back to Random): {}", e.getMessage());
        }

        // 2. Fallback to Random if AI failed or missing essential items
        if (recommendedTop == null || recommendedBottom == null) {
            recommendedTop = getRandomItem(tops);
            recommendedBottom = getRandomItem(bottoms);

            if (season == Season.WINTER || season == Season.FALL || season == Season.ALL) {
                if (!outers.isEmpty()) {
                    recommendedOuter = getRandomItem(outers);
                }
            }
            recommendedReason = "랜덤 추천 (AI 응답 실패 또는 조건 미충족)";
        }

        // Save History
        FitHistory history = FitHistory.builder()
                .user(user)
                .top(recommendedTop)
                .bottom(recommendedBottom)
                .outer(recommendedOuter)
                .place(request.place())
                .mood(request.mood() != null ? request.mood() : "Daily") // Default if null
                .season(season.name())
                .recommendationReason(recommendedReason)
                .build();

        fitHistoryRepository.save(history);

        return FitResponseDto.of(request.place(), request.mood() != null ? request.mood() : "Daily", season.name(),
                recommendedTop, recommendedBottom, recommendedOuter, recommendedReason);
    }

    private WardrobeItem findItemById(List<WardrobeItem> items, Long id) {
        return items.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public java.util.List<com.fittim.backend.dto.FitHistoryDto> getFitHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Fetch top 10
        return fitHistoryRepository
                .findTopByUserIdOrderByCreatedAtDesc(user.getId(),
                        org.springframework.data.domain.PageRequest.of(0, 10))
                .stream()
                .map(com.fittim.backend.dto.FitHistoryDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public com.fittim.backend.dto.FitHistoryDetailDto getFitHistoryDetail(Long id) {
        FitHistory history = fitHistoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fit history not found"));
        return com.fittim.backend.dto.FitHistoryDetailDto.from(history);
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
