package com.fittim.backend.controller;

import com.fittim.backend.dto.AuthDto.*;
import com.fittim.backend.dto.JwtResponse;
import com.fittim.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send-verification-code")
    public ResponseEntity<String> sendVerificationCode(@RequestBody @Valid SendCodeRequest request) {
        authService.sendVerificationCode(request);
        return ResponseEntity.ok("Verification code sent");
    }

    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestBody @Valid VerifyCodeRequest request) {
        authService.verifyCode(request);
        return ResponseEntity.ok("Code verified");
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody @Valid SignupRequest request) {
        Long userId = authService.signup(request);
        return ResponseEntity.created(URI.create("/api/users/" + userId)).build();
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody @Valid LoginRequest request) {
        JwtResponse token = authService.login(request);
        return ResponseEntity.ok(token);
    }
}
