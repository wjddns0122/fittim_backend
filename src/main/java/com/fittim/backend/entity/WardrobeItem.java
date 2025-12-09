package com.fittim.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "wardrobe_item", indexes = {
        @Index(name = "idx_wardrobe_user_season", columnList = "user_id, season"),
        @Index(name = "idx_wardrobe_user_category", columnList = "user_id, category")
})
public class WardrobeItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private Season season;

    private String imageUrl;

    @Column
    private String name;

    @Column
    private String brand;

    @Column
    private String colors; // Comma-separated

    @Builder
    public WardrobeItem(User user, Category category, Season season, String imageUrl, String name, String brand,
            String colors) {
        this.user = user;
        this.category = category;
        this.season = season;
        this.imageUrl = imageUrl;
        this.name = name;
        this.brand = brand;
        this.colors = colors;
    }

    public void update(String name, String brand, String colors, Category category, Season season) {
        this.name = name;
        this.brand = brand;
        this.colors = colors;
        this.category = category;
        this.season = season;
    }

    public void patch(String name, String brand, String colors, Category category, Season season) {
        if (name != null)
            this.name = name;
        if (brand != null)
            this.brand = brand;
        if (colors != null)
            this.colors = colors;
        if (category != null)
            this.category = category;
        if (season != null)
            this.season = season;
    }
}
