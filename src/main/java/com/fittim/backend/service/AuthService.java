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

        // For development/debugging: Print verification code to console
        log.info("Sent verification code to {}: {}", normalizedEmail, code);
        // KEEP this System.out as requested by user for explicit console visibility
        // during dev
        System.out.println("Verification code for " + normalizedEmail + ": " + code);
    }

    public boolean verifyCode(VerifyCodeRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        String normalizedCode = request.code().trim();

        log.debug("Verifying code for {}: {}", normalizedEmail, normalizedCode);

        VerificationInfo info = verificationCodes.get(normalizedEmail);

        if (info == null) {
            log.warn("Verification failed: Code not found or expired for {}", normalizedEmail);
            throw new IllegalArgumentException("Verification code not found or expired for " + normalizedEmail);
        }

        if (System.currentTimeMillis() > info.expirationTime()) {
            verificationCodes.remove(normalizedEmail);
            log.warn("Verification failed: Code expired for {}", normalizedEmail);
            throw new IllegalArgumentException("Verification code expired");
        }

        if (!info.code().equals(normalizedCode)) {
            log.warn("Verification failed: Invalid code for {}", normalizedEmail);
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
                    log.warn("Login failed: User not found for email {}", request.email());
                    return new IllegalArgumentException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Login failed: Password mismatch for email {}", request.email());
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtTokenProvider.createToken(user.getEmail(), user.getRole());
        return new JwtResponse(token);
    }

    private String generateRandomCode() {
        return String.format("%06d", new Random().nextInt(1000000));
    }
}
