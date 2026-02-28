package com.univerliga.analytics.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.univerliga.analytics.error.ApiErrorResponse;
import com.univerliga.analytics.util.RequestIdHolder;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/v1/reports/**").hasAnyRole("ADMIN", "MANAGER", "HR")
                .requestMatchers("/api/v1/**").authenticated()
                .anyRequest().permitAll());
        http.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(new JwtRealmRoleConverter())));
        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> writeError(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", objectMapper))
                .accessDeniedHandler((request, response, accessDeniedException) -> writeError(response, HttpStatus.FORBIDDEN, "FORBIDDEN", objectMapper)));
        return http.build();
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String code, ObjectMapper objectMapper) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiErrorResponse body = ApiErrorResponse.of(code, status.getReasonPhrase(), List.of(), RequestIdHolder.get());
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
