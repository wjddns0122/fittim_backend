package com.fittim.backend.repository;

import com.fittim.backend.entity.Season;
import com.fittim.backend.entity.User;
import com.fittim.backend.entity.WardrobeItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WardrobeItemRepository extends JpaRepository<WardrobeItem, Long> {
    List<WardrobeItem> findAllByUserOrderByCreatedAtDesc(User user);

    List<WardrobeItem> findAllByUserAndSeason(User user, Season season);
}
