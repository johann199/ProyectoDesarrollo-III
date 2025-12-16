package com.sgl.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import com.sgl.backend.dto.LoginRequest;
import com.sgl.backend.dto.LoginResponse;
import com.sgl.backend.dto.SiraLoginRequest;
import com.sgl.backend.dto.SiraLoginResponse;
import com.sgl.backend.dto.SiraUserInfo;
import com.sgl.backend.entity.Role;
import com.sgl.backend.entity.User;
import com.sgl.backend.exception.SglAuthException;
import com.sgl.backend.repository.RoleRepository;
import com.sgl.backend.repository.UserRepository;
import com.sgl.backend.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${sira.base-url}")
    private String siraBaseUrl;

    public LoginResponse authenticate(LoginRequest loginRequest) {
        String code = loginRequest.getCode();
        String password = loginRequest.getPassword();

        if ("admin_code".equals(code)) {
            return authenticateAdmin(code, password);
        }

        SiraLoginResponse siraResponse = authenticateWithSira(code, password);
        SiraUserInfo userInfo = fetchSiraUserInfo(siraResponse.getToken(), siraResponse.isStudent());

        User user = upsertUser(userInfo, siraResponse.isStudent());

        String token = jwtService.generateToken(user);
        return new LoginResponse(token, user.getRole().getName());
    }

    private SiraLoginResponse authenticateWithSira(String code, String password) {
        try {
            String url = siraBaseUrl + "/auth";
            SiraLoginRequest siraRequest = new SiraLoginRequest(code, password);
            ResponseEntity<SiraLoginResponse> response = restTemplate.postForEntity(url, siraRequest, SiraLoginResponse.class);
            if (response.getBody() == null) {
                throw new RuntimeException("SIRA login failed: No response body");
            }
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new SglAuthException("SIRA authentication failed: Invalid credentials", e);
        } catch (Exception e) {
            throw new SglAuthException("SIRA authentication error: " + e.getMessage(), e);
        }
    }

    private SiraUserInfo fetchSiraUserInfo(String token, boolean isStudent) {
        try {
            String endpoint = isStudent ? "/student/info" : "/teacher/info";
            String url = siraBaseUrl + endpoint;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<SiraUserInfo> response = restTemplate.exchange(url, HttpMethod.GET, entity, SiraUserInfo.class);
            if (response.getBody() == null) {
                throw new SglAuthException("SIRA user info failed: No response body");
            }
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new SglAuthException("SIRA user info failed: Invalid token", e);
        } catch (Exception e) {
            throw new SglAuthException("SIRA user info error: " + e.getMessage(), e);
        }
    }

    private User upsertUser(SiraUserInfo userInfo, boolean isStudent) {
        String defaultRoleName = isStudent ? "ESTUDIANTE" : "DOCENTE";
        User user = userRepository.findById(userInfo.getCode()).orElse(null);
        Role role;

        if (user == null) {
            role = roleRepository.findByName(defaultRoleName)
                    .orElseThrow(() -> new SglAuthException("Role not found: " + defaultRoleName));
            user = User.builder()
                    .code(userInfo.getCode())
                    .name(userInfo.getName())
                    .email(userInfo.getEmail())
                    .document(userInfo.getDocument())
                    .role(role)
                    .build();
        } else {
            user.setName(userInfo.getName());
            user.setEmail(userInfo.getEmail());
            user.setDocument(userInfo.getDocument());
        }

        return userRepository.save(user);
    }

    private LoginResponse authenticateAdmin(String code, String password) {
        User admin = userRepository.findById(code)
                .orElseThrow(() -> new SglAuthException("Admin user not found"));

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new SglAuthException("Invalid admin credentials");
        }

        String token = jwtService.generateToken(admin);
        return new LoginResponse(token, admin.getRole().getName());
    }
}
