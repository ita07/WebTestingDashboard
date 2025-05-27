package com.ita07.webTestingDashboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve files from the screenshots/ directory at /screenshots/**
        registry.addResourceHandler("/screenshots/**")
                .addResourceLocations("file:screenshots/");
    }
}

