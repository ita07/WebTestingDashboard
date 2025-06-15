package com.ita07.webTestingDashboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Convert relative paths to absolute paths for better reliability
        String screenshotsPath = new File("screenshots").getAbsolutePath();
        String reportsPath = new File("reports").getAbsolutePath();

        // Ensure paths end with separator
        if (!screenshotsPath.endsWith(File.separator)) {
            screenshotsPath += File.separator;
        }
        if (!reportsPath.endsWith(File.separator)) {
            reportsPath += File.separator;
        }

        // Configure resource handlers with absolute paths
        registry.addResourceHandler("/screenshots/**")
                .addResourceLocations("file:" + screenshotsPath);

        registry.addResourceHandler("/reports/**")
                .addResourceLocations("file:" + reportsPath);
    }

    @Bean
    public LayoutInterceptor layoutInterceptor() {
        return new LayoutInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(layoutInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/error", "/api/**", "/static/**", "/screenshots/**", "/reports/**");
    }
}
