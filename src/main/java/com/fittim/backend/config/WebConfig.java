package com.fittim.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:*") // "http://localhost:*" 패턴 허용
                .allowedMethods("*") // 모든 HTTP 메소드 허용 (GET, POST, PUT, DELETE, OPTIONS 등)
                .allowCredentials(true); // 쿠키/인증 정보 포함 허용
    }
}
