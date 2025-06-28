package com.ita07.webTestingDashboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.Customizer;

@Configuration
public class SecurityConfig {
    @Value("${dashboard.security.username}")
    private String username;
    @Value("${dashboard.security.password}")
    private String password;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for APIs and non-browser clients
                .csrf(AbstractHttpConfigurer::disable)
                // Configure endpoint authorization
                .authorizeHttpRequests(authz -> authz
                        // Public resources and documentation
                        .requestMatchers(
                            "/screenshots/**",
                            "/icons/**",
                            "/reports/**",
                            "/reports-viewer/**",
                            "/swagger-ui/**",
                            "/v3/api-docs/**"
                        ).permitAll()
                        // Require authentication for all API endpoints
                        .requestMatchers("/api/**").authenticated()
                        // Require authentication for all other endpoints (web UI)
                        .anyRequest().authenticated()
                )
                // Enable HTTP Basic for API endpoints (for Swagger UI and programmatic access)
                .httpBasic(Customizer.withDefaults())
                // Enable form-based login for web UI
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                // Custom authentication entry point:
                // - For /api/** endpoints, return 401 Unauthorized (no login page or browser popup)
                // - For all other endpoints, redirect to the login page
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            String uri = request.getRequestURI();
                            if (uri.startsWith("/api/")) {
                                response.sendError(401, "Unauthorized");
                            } else {
                                response.sendRedirect("/login");
                            }
                        })
                );
        return http.build();
    }

    @Bean
    public UserDetailsService users(PasswordEncoder passwordEncoder) {
        UserDetails user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
