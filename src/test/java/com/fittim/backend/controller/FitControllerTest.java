package com.fittim.backend.controller;

import com.fittim.backend.config.JwtTokenProvider;
import com.fittim.backend.dto.FitHistoryDto;
import com.fittim.backend.service.FitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FitController.class)
class FitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FitService fitService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider; // Required for Security Config

    @Test
    @WithMockUser(username = "test@example.com")
    void getFitHistory_ShouldReturnHasReasonField() throws Exception {
        // Given
        FitHistoryDto mockHistory = new FitHistoryDto(
                1L,
                "오늘의 TEST 룩",
                "TEST_PLACE",
                "TEST_MOOD",
                "WINTER",
                "http://image.url",
                "THIS_IS_THE_REASON_FROM_BACKEND", // The critical field
                LocalDateTime.now().toString());

        given(fitService.getFitHistory("test@example.com"))
                .willReturn(List.of(mockHistory));

        // When & Then
        mockMvc.perform(get("/api/fits/history"))
                .andDo(print()) // Print result to console
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reason").value("THIS_IS_THE_REASON_FROM_BACKEND")); // Verify JSON key is
                                                                                              // 'reason'
    }
}
