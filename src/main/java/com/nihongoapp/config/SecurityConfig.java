package com.nihongoapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    private final LoggingMdcFilter loggingMdcFilter;
    private final JwtAuthFilter jwtAuthFilter;
    private final BannedUserFilter bannedUserFilter;

    public SecurityConfig(LoggingMdcFilter loggingMdcFilter,
                          JwtAuthFilter jwtAuthFilter,
                          BannedUserFilter bannedUserFilter) {
        this.loggingMdcFilter = loggingMdcFilter;
        this.jwtAuthFilter = jwtAuthFilter;
        this.bannedUserFilter = bannedUserFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/", "/index.html", "/error").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/v1/learning/**").authenticated()
                .requestMatchers("/api/v1/wallet/**").authenticated()
                .requestMatchers("/api/v1/gacha/**").authenticated()
                .requestMatchers("/api/v1/iap/**").authenticated()
                .requestMatchers("/api/v1/lessons/**").authenticated()
                .requestMatchers("/api/v1/shop/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
                .requestMatchers("/internal/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(loggingMdcFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(bannedUserFilter, BasicAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
