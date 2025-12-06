package com.fittim.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDto {

        public record SendCodeRequest(
                        @NotBlank @Email String email) {
        }

        public record VerifyCodeRequest(
                        @NotBlank @Email String email,
                        @NotBlank String code) {
        }

        public record SignupRequest(
                        @NotBlank @Email String email,
                        @NotBlank String password,
                        @NotBlank String nickname) {
        }

        public record LoginRequest(
                        @NotBlank @Email String email,
                        @NotBlank String password) {
        }
}
