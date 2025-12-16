package com.sgl.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

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

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(restTemplate, userRepository, roleRepository, passwordEncoder, jwtService);
        setField(authService, "siraBaseUrl", "http://localhost:3000/api");
    }

    @Test
    void authenticate_admin_success() {
        LoginRequest request = new LoginRequest("admin_code", "admin_password");
        Role adminRole = Role.builder().id(1L).name("ADMIN").build();
        User admin = User.builder().code("admin_code").password("hashed_password").role(adminRole).build();
        when(userRepository.findById("admin_code")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("admin_password", "hashed_password")).thenReturn(true);
        when(jwtService.generateToken(admin)).thenReturn("jwt_admin_token");

        LoginResponse response = authService.authenticate(request);

        assertThat(response.getToken()).isEqualTo("jwt_admin_token");
        assertThat(response.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void authenticate_admin_invalidPassword_throwsException() {
        LoginRequest request = new LoginRequest("admin_code", "wrong_password");
        Role adminRole = Role.builder().id(1L).name("ADMIN").build();
        User admin = User.builder().code("admin_code").password("hashed_password").role(adminRole).build();
        when(userRepository.findById("admin_code")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("wrong_password", "hashed_password")).thenReturn(false);

        assertThrows(SglAuthException.class, () -> {
            authService.authenticate(request);
        }, "Invalid admin credentials");
    }

    @Test
    void authenticate_student_success() {
        LoginRequest request = new LoginRequest("12345", "password");
        SiraLoginResponse siraResponse = new SiraLoginResponse("sira_token", true);
        SiraUserInfo userInfo = new SiraUserInfo("3743", "John Doe", "12345", "john@correounivalle.edu.co", "123456789");
        Role studentRole = Role.builder().id(1L).name("ESTUDIANTE").build();
        User user = User.builder().code("12345").name("John Doe").email("john@correounivalle.edu.co").document("123456789").role(studentRole).build();

        when(restTemplate.postForEntity(eq("http://localhost:3000/api/auth"), any(SiraLoginRequest.class), eq(SiraLoginResponse.class)))
                .thenReturn(ResponseEntity.ok(siraResponse));
        when(restTemplate.exchange(eq("http://localhost:3000/api/student/info"), eq(HttpMethod.GET), any(HttpEntity.class), eq(SiraUserInfo.class)))
                .thenReturn(ResponseEntity.ok(userInfo));
        when(userRepository.findById("12345")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ESTUDIANTE")).thenReturn(Optional.of(studentRole));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("jwt_student_token");

        LoginResponse response = authService.authenticate(request);

        assertThat(response.getToken()).isEqualTo("jwt_student_token");
        assertThat(response.getRole()).isEqualTo("ESTUDIANTE");
        verify(userRepository).save(any(User.class));
    }  

    @Test
    void authenticate_siraInvalidCredentials_throwsException() {
        LoginRequest request = new LoginRequest("12345", "wrong_password");
        when(restTemplate.postForEntity(eq("http://localhost:3000/api/auth"), any(SiraLoginRequest.class), eq(SiraLoginResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        
                assertThrows(SglAuthException.class, () -> {
            authService.authenticate(request);
        }, "SIRA authentication failed: Invalid credentials");
    }

    @Test
    void upsertUser_existingUser_updatesInfo() {
        SiraUserInfo userInfo = new SiraUserInfo("3743", "Jane Doe", "54321", "jane@correo.edu", "987654321");
        Role docenteRole = Role.builder().id(2L).name("DOCENTE").build();
        User existingUser = User.builder()
                .code("54321").name("Old Name").email("old@email").document("111").role(docenteRole).build();

        when(userRepository.findById("54321")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        setField(authService, "siraBaseUrl", "http://localhost:3000/api");

        User updatedUser = invokeUpsertUser(userInfo, false);

        assertThat(updatedUser.getName()).isEqualTo("Jane Doe");
        assertThat(updatedUser.getEmail()).isEqualTo("jane@correo.edu");
        assertThat(updatedUser.getDocument()).isEqualTo("987654321");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void upsertUser_roleNotFound_throwsException() {
        SiraUserInfo userInfo = new SiraUserInfo("3743", "Carlos", "99999", "carlos@correo.edu", "000111222");
        when(userRepository.findById("99999")).thenReturn(Optional.empty());
        when(roleRepository.findByName("DOCENTE")).thenReturn(Optional.empty());

        assertThrows(SglAuthException.class, () -> invokeUpsertUser(userInfo, false),
                "Role not found: DOCENTE");
    }

    @Test
    void fetchSiraUserInfo_invalidToken_throwsException() {
        when(restTemplate.exchange(
                eq("http://localhost:3000/api/teacher/info"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(SiraUserInfo.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        assertThrows(SglAuthException.class, () -> invokeFetchSiraUserInfo("bad_token", false),
                "SIRA user info failed: Invalid token");
    }

    @Test
    void fetchSiraUserInfo_nullBody_throwsException() {
        when(restTemplate.exchange(
                eq("http://localhost:3000/api/student/info"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(SiraUserInfo.class)
        )).thenReturn(ResponseEntity.ok(null));

        assertThrows(SglAuthException.class, () -> invokeFetchSiraUserInfo("token", true),
                "SIRA user info failed: No response body");
    }

    @Test
    void authenticateWithSira_nullBody_throwsException() {
        when(restTemplate.postForEntity(
                eq("http://localhost:3000/api/auth"),
                any(SiraLoginRequest.class),
                eq(SiraLoginResponse.class))
        ).thenReturn(ResponseEntity.ok(null));

        assertThrows(SglAuthException.class, () -> invokeAuthenticateWithSira("code", "pass"));
    }

    private User invokeUpsertUser(SiraUserInfo userInfo, boolean isStudent) {
        try {
            var method = AuthService.class.getDeclaredMethod("upsertUser", SiraUserInfo.class, boolean.class);
            method.setAccessible(true);
            return (User) method.invoke(authService, userInfo, isStudent);
        } catch (Exception e) {
            if (e.getCause() instanceof RuntimeException) throw (RuntimeException) e.getCause();
            throw new RuntimeException(e);
        }
    }

    private SiraUserInfo invokeFetchSiraUserInfo(String token, boolean isStudent) {
        try {
            var method = AuthService.class.getDeclaredMethod("fetchSiraUserInfo", String.class, boolean.class);
            method.setAccessible(true);
            return (SiraUserInfo) method.invoke(authService, token, isStudent);
        } catch (Exception e) {
            if (e.getCause() instanceof RuntimeException) throw (RuntimeException) e.getCause();
            throw new RuntimeException(e);
        }
    }

    private SiraLoginResponse invokeAuthenticateWithSira(String code, String password) {
        try {
            var method = AuthService.class.getDeclaredMethod("authenticateWithSira", String.class, String.class);
            method.setAccessible(true);
            return (SiraLoginResponse) method.invoke(authService, code, password);
        } catch (Exception e) {
            if (e.getCause() instanceof RuntimeException) throw (RuntimeException) e.getCause();
            throw new RuntimeException(e);
        }
    }


    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
