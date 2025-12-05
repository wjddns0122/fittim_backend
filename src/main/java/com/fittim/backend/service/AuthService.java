package com.fittim.backend.service;

import com.fittim.backend.config.JwtTokenProvider;
import com.fittim.backend.dto.LoginRequest;
import com.fittim.backend.dto.SignupRequest;
import com.fittim.backend.dto.JwtResponse;
import com.fittim.backend.entity.User;
import com.fittim.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public Long signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists: " + request.email());
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .role("ROLE_USER")
                .build();

        return userRepository.save(user).getId();
    }

    @Transactional(readOnly = true)
    public JwtResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    System.out.println("Login failed: User not found for email " + request.email());
                    return new IllegalArgumentException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            System.out.println("Login failed: Password mismatch for email " + request.email());
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtTokenProvider.createToken(user.getEmail(), user.getRole());
        return new JwtResponse(token);
    }
}
