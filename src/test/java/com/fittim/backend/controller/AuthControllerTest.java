package com.fittim.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fittim.backend.dto.AuthDto.SendCodeRequest;
import com.fittim.backend.dto.AuthDto.SignupRequest;
import com.fittim.backend.dto.AuthDto.VerifyCodeRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for this test
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private com.fittim.backend.config.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Test Case 1: Send Verification Code Successful - 200 OK")
    void sendVerificationCode_Success() throws Exception {
        // Given
        SendCodeRequest request = new SendCodeRequest("test@example.com");

        // When & Then
        mockMvc.perform(post("/api/auth/send-verification-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test Case 2: Verify Code Failed (Wrong Code) - 400 Bad Request or 401")
    void verifyCode_Failed() throws Exception {
        // Given
        VerifyCodeRequest request = new VerifyCodeRequest("test@example.com", "WRONG_CODE");

        // When & Then
        // GlobalExceptionHandler handles IllegalArgumentException as 400 or 401
        // depending on message.
        // If message is generic, it might be 400.
        mockMvc.perform(post("/api/auth/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Expecting 400 per GlobalExceptionHandler for generic
                                                     // IllegalArgumentException
    }

    @Test
    @DisplayName("Test Case 3: Signup Failed (Missing Fields) - 400 Bad Request")
    void signup_Failed_Validation() throws Exception {
        // Given
        // Email is invalid (empty), Password is valid, Nickname is valid
        SignupRequest request = new SignupRequest("", "password123", "nickname");

        // When & Then
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
