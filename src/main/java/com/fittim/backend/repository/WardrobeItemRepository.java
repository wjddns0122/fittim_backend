package com.fittim.backend.repository;

import com.fittim.backend.entity.Season;
import com.fittim.backend.entity.User;
import com.fittim.backend.entity.WardrobeItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WardrobeItemRepository extends JpaRepository<WardrobeItem, Long> {
        List<WardrobeItem> findAllByUserOrderByCreatedAtDesc(User user);

        List<WardrobeItem> findAllByUserAndSeason(User user, Season season);

        @org.springframework.data.jpa.repository.Query("SELECT w FROM WardrobeItem w WHERE w.user.id = :userId " +
                        "AND (:category IS NULL OR w.category = :category) " +
                        "AND (:season IS NULL OR w.season = :season) " +
                        "ORDER BY w.createdAt DESC")
        List<WardrobeItem> findByUserIdAndCategoryAndSeason(
                        @org.springframework.data.repository.query.Param("userId") Long userId,
                        @org.springframework.data.repository.query.Param("category") com.fittim.backend.entity.Category category,
                        @org.springframework.data.repository.query.Param("season") Season season);

        List<WardrobeItem> findByUserIdAndCategory(Long userId, com.fittim.backend.entity.Category category);

        List<WardrobeItem> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
}
