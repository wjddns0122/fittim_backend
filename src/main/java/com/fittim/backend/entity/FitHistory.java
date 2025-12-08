package com.fittim.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "fit_history", indexes = {
        @Index(name = "idx_fit_history_user_created_at", columnList = "user_id, created_at")
})
public class FitHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "top_id")
    private WardrobeItem top;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bottom_id")
    private WardrobeItem bottom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outer_id")
    private WardrobeItem outer;

    private String place; // e.g. "CAMPUS", "CAFE"
    private String mood; // e.g. "CASUAL", "MINIMAL" (currently from request or derived)

    @Column(length = 1000)
    private String reason;
}
