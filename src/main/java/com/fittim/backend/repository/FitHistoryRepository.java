package com.fittim.backend.repository;

import com.fittim.backend.entity.FitHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FitHistoryRepository extends JpaRepository<FitHistory, Long> {

    @Query("SELECT fh FROM FitHistory fh JOIN FETCH fh.top JOIN FETCH fh.bottom LEFT JOIN FETCH fh.outer WHERE fh.user.id = :userId ORDER BY fh.createdAt DESC")
    List<FitHistory> findTopByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);
}
