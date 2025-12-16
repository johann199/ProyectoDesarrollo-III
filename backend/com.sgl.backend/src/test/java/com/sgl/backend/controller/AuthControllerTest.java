package com.sgl.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgl.backend.exception.SglAuthException;
import com.sgl.backend.security.JwtAuthenticationFilter;
import com.sgl.backend.security.JwtService;
import com.sgl.backend.dto.LoginRequest;
import com.sgl.backend.dto.LoginResponse;
import com.sgl.backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter; 

    @Autowired
    private AuthService authService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        AuthService authService() {
            return mock(AuthService.class);
        }
    }

    @Test
    void login_success_returnsTokenAndRole() throws Exception {
        LoginRequest request = new LoginRequest("12345", "password");
        LoginResponse response = new LoginResponse("jwt_token", "ESTUDIANTE");
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt_token"))
                .andExpect(jsonPath("$.role").value("ESTUDIANTE"));
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        LoginRequest request = new LoginRequest("12345", "wrong_password");
        when(authService.authenticate(any(LoginRequest.class)))
                .thenThrow(new SglAuthException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void login_unexpectedError_returns500() throws Exception {
        LoginRequest request = new LoginRequest("12345", "password");
        when(authService.authenticate(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
