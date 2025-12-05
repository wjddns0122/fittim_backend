package com.fittim.backend.config;

import com.fittim.backend.entity.User;
import com.fittim.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TestUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String email = "test@example.com";
        if (userRepository.existsByEmail(email)) {
            log.info("Test user already exists.");
            return;
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode("1234"))
                .nickname("테스트유저")
                .role("ROLE_USER")
                .build();

        userRepository.save(user);
        log.info("Created test user: {}", email);
    }
}
