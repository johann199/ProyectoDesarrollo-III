package com.sgl.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.sgl.backend.security.JwtAuthenticationFilter;
import com.sgl.backend.security.JwtService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration 
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService);

        http
            .cors(cors -> {})
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/users/**", "/api/roles/**").hasAuthority("ADMIN")
                .requestMatchers("/api/attendances/**").hasAnyAuthority("ADMIN", "MONITOR")
                .requestMatchers(HttpMethod.POST, "/api/laboratories").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/laboratories/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/laboratories").hasAnyAuthority("ADMIN", "DOCENTE")
                .requestMatchers("/api/practices/**").hasAnyAuthority("ADMIN", "DOCENTE")
                .requestMatchers("/api/loans").hasAuthority("MONITOR")
                .requestMatchers("/api/loans/my-active").hasAuthority("ESTUDIANTE")
                .requestMatchers("/api/loans/active").hasAnyAuthority("ADMIN", "MONITOR")
                .requestMatchers("/api/loans/*/return").hasAuthority("MONITOR")
                .requestMatchers("/api/monitor-attendance").hasAuthority("MONITOR")
                .requestMatchers("/api/monitor-attendance/report/**").hasAuthority("ADMIN")
                .requestMatchers("/api/attendances/**").hasAnyAuthority("ADMIN", "MONITOR")
                .requestMatchers("/api/attendances/report**").hasAuthority("ADMIN")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Unauthorized\"}");
                })
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
