package com.sgl.backend.security;

import com.sgl.backend.entity.Role;
import com.sgl.backend.entity.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    
    @InjectMocks
    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        setField(jwtService, "secret", "oSiqq2BB/4GXGBxfOpt7Vmu9vugaBq1BxeUP+3dst6k=");
        setField(jwtService, "expirationMs", 3600000L);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = JwtService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void generateToken_createsValidToken() {
        Role role = Role.builder().name("ADMIN").build();
        User user = User.builder().code("admin_code").role(role).build();

        String token = jwtService.generateToken(user);

        assertThat(token).isNotEmpty();
        Claims claims = jwtService.extractClaims(token);
        assertThat(claims.getSubject()).isEqualTo("admin_code");
        assertThat(claims.get("role")).isEqualTo("ADMIN");
        assertThat(claims.getExpiration()).isNotNull();
    }

    @Test
    void extractCode_returnsCorrectCode() {
        Role role = Role.builder().name("ADMIN").build();
        User user = User.builder().code("admin_code").role(role).build();
        String token = jwtService.generateToken(user);

        String code = jwtService.extractCode(token);

        assertThat(code).isEqualTo("admin_code");
    }

    @Test
    void extractRole_returnsCorrectRole() {
        Role role = Role.builder().name("ADMIN").build();
        User user = User.builder().code("admin_code").role(role).build();
        String token = jwtService.generateToken(user);

        String roleName = jwtService.extractRole(token);

        assertThat(roleName).isEqualTo("ADMIN");
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        Role role = Role.builder().name("ADMIN").build();
        User user = User.builder().code("admin_code").role(role).build();
        String token = jwtService.generateToken(user);

        boolean isValid = jwtService.isTokenValid(token);

        assertTrue(isValid);
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() throws Exception {
        setField(jwtService, "expirationMs", -1000L); 
        Role role = Role.builder().name("ADMIN").build();
        User user = User.builder().code("admin_code").role(role).build();
        String token = jwtService.generateToken(user);

        boolean isValid = jwtService.isTokenValid(token);

        assertFalse(isValid);
    }

    @Test
    void isTokenValid_invalidToken_returnsFalse() {
        boolean isValid = jwtService.isTokenValid("invalid_token");
        
        assertFalse(isValid);
    }
}
