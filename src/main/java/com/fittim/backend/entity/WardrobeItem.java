package com.fittim.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.ArrayList;
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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "wardrobe_item_colors", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "color")
    private List<String> colors;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "wardrobe_item_seasons", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "season")
    private List<String> seasons;

    @Builder
    public WardrobeItem(User user, Category category, Season season, String imageUrl, String name, String brand,
            List<String> userSeasons, List<String> colors) {
        this.user = user;
        this.category = category;
        this.season = season;
        this.imageUrl = imageUrl;
        this.name = name;
        this.brand = brand;
        this.seasons = userSeasons;
        this.colors = colors;
    }

    public void update(String name, String brand, List<String> colors, Category category, Season season,
            List<String> seasons) {
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.season = season;

        if (this.colors == null)
            this.colors = new ArrayList<>();
        this.colors.clear();
        if (colors != null)
            this.colors.addAll(colors);

        if (this.seasons == null)
            this.seasons = new ArrayList<>();
        this.seasons.clear();
        if (seasons != null)
            this.seasons.addAll(seasons);
    }

    public void patch(String name, String brand, List<String> colors, Category category, Season season,
            List<String> seasons) {
        if (name != null)
            this.name = name;
        if (brand != null)
            this.brand = brand;
        if (category != null)
            this.category = category;
        if (season != null)
            this.season = season;

        if (colors != null) {
            if (this.colors == null)
                this.colors = new ArrayList<>();
            this.colors.clear();
            this.colors.addAll(colors);
        }

        if (seasons != null) {
            if (this.seasons == null)
                this.seasons = new ArrayList<>();
            this.seasons.clear();
            this.seasons.addAll(seasons);
        }
    }

}
