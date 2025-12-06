package com.fittim.backend.service;

import com.fittim.backend.config.JwtTokenProvider;
import com.fittim.backend.dto.AuthDto.*;
import com.fittim.backend.dto.JwtResponse;
import com.fittim.backend.entity.User;
import com.fittim.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // In-memory storage for verification codes: Email -> Code info
    private final Map<String, VerificationInfo> verificationCodes = new ConcurrentHashMap<>();

    private record VerificationInfo(String code, long expirationTime) {
    }

    public void sendVerificationCode(SendCodeRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already exists: " + normalizedEmail);
        }

        String code = generateRandomCode();
        long expirationTime = System.currentTimeMillis() + 5 * 60 * 1000; // 5 minutes validity
        verificationCodes.put(normalizedEmail, new VerificationInfo(code, expirationTime));

        // In a real app, send email here. For now, print to console.
        log.info("Verification code for {}: {}", normalizedEmail, code);
        System.out.println("Verification code for " + normalizedEmail + ": " + code);
    }

    public boolean verifyCode(VerifyCodeRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        String normalizedCode = request.code().trim();

        System.out.println("--- Verify Code Debug ---");
        System.out.println("Raw Email: [" + request.email() + "], Normalized: [" + normalizedEmail + "]");
        System.out.println("Raw Code: [" + request.code() + "], Normalized: [" + normalizedCode + "]");

        VerificationInfo info = verificationCodes.get(normalizedEmail);
        System.out.println("Stored Info: " + (info != null ? info.code() : "null"));
        System.out.println("Current Keys: " + verificationCodes.keySet());
        System.out.println("-------------------------");

        if (info == null) {
            throw new IllegalArgumentException("Verification code not found or expired for " + normalizedEmail);
        }

        if (System.currentTimeMillis() > info.expirationTime()) {
            verificationCodes.remove(normalizedEmail);
            throw new IllegalArgumentException("Verification code expired");
        }

        if (!info.code().equals(normalizedCode)) {
            throw new IllegalArgumentException(
                    "Invalid verification code. Expected: " + info.code() + ", Got: " + normalizedCode);
        }

        verificationCodes.put(normalizedEmail,
                new VerificationInfo("VERIFIED", System.currentTimeMillis() + 10 * 60 * 1000)); // Verified status valid
                                                                                                // for 10 mins

        return true;
    }

    @Transactional
    public Long signup(SignupRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        // Double check verification status
        VerificationInfo info = verificationCodes.get(normalizedEmail);
        if (info == null || !"VERIFIED".equals(info.code())) {
            throw new IllegalArgumentException("Email not verified");
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already exists: " + normalizedEmail);
        }

        User user = User.builder()
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .role("ROLE_USER")
                .build();

        verificationCodes.remove(normalizedEmail); // Clean up

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

    private String generateRandomCode() {
        return String.format("%06d", new Random().nextInt(1000000));
    }
}
