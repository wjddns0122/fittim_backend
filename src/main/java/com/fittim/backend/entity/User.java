package com.fittim.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String role; // "ROLE_USER", "ROLE_ADMIN"

    @Column
    private Double height;

    @Column
    private Double weight;

    @Column
    private String bodyType;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_styles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "style")
    private List<String> preferredStyles;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_malls", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "mall")
    private List<String> preferredMalls;

    public void updateProfile(Double height, Double weight, String bodyType, Gender gender,
            List<String> preferredStyles, List<String> preferredMalls) {
        if (height != null)
            this.height = height;
        if (weight != null)
            this.weight = weight;
        if (bodyType != null)
            this.bodyType = bodyType;
        if (gender != null)
            this.gender = gender;

        if (preferredStyles != null) {
            if (this.preferredStyles == null)
                this.preferredStyles = new ArrayList<>();
            this.preferredStyles.clear();
            this.preferredStyles.addAll(preferredStyles);
        }

        if (preferredMalls != null) {
            if (this.preferredMalls == null)
                this.preferredMalls = new ArrayList<>();
            this.preferredMalls.clear();
            this.preferredMalls.addAll(preferredMalls);
        }
    }

    public void patchProfile(Double height, Double weight, String bodyType, Gender gender,
            List<String> preferredStyles, List<String> preferredMalls) {
        if (height != null)
            this.height = height;
        if (weight != null)
            this.weight = weight;
        if (bodyType != null)
            this.bodyType = bodyType;
        if (gender != null)
            this.gender = gender;
        if (preferredStyles != null) {
            if (this.preferredStyles == null)
                this.preferredStyles = new ArrayList<>();
            this.preferredStyles.clear();
            this.preferredStyles.addAll(preferredStyles);
        }
        if (preferredMalls != null) {
            if (this.preferredMalls == null)
                this.preferredMalls = new ArrayList<>();
            this.preferredMalls.clear();
            this.preferredMalls.addAll(preferredMalls);
        }
    }

    @Builder
    public User(String email, String password, String nickname, String role, Double height, Double weight,
            String bodyType, Gender gender, List<String> preferredStyles, List<String> preferredMalls) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
        this.height = height;
        this.weight = weight;
        this.bodyType = bodyType;
        this.gender = gender;
        this.preferredStyles = preferredStyles;
        this.preferredMalls = preferredMalls;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
