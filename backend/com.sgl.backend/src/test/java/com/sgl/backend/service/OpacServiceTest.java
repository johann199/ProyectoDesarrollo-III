package com.sgl.backend.service;

import com.sgl.backend.dto.OpacUserInfo;
import com.sgl.backend.exception.SglException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpacServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OpacService opacService;

    @BeforeEach
    void setUp() {
        setField(opacService, "opacBaseUrl", "http://localhost:3001/api");
    }

    @Test
    void fetchUserInfo_success() {
        String code = "2025001";
        ResponseEntity<OpacUserInfo> response = ResponseEntity.ok(new OpacUserInfo("John Doe", "3743", "12345", null, null));

        when(restTemplate.exchange(
                eq("http://localhost:3001/api/student/preview/" + code),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(OpacUserInfo.class)
        )).thenReturn(response);

        OpacUserInfo result = opacService.fetchUserInfo(code);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("John Doe");
    }

    @Test
    void fetchUserInfo_nullBody_throwsException() {
        String code = "2025002";
        when(restTemplate.exchange(
                eq("http://localhost:3001/api/student/preview/" + code),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(OpacUserInfo.class)
        )).thenReturn(ResponseEntity.ok(null));

        SglException ex = assertThrows(SglException.class, () -> opacService.fetchUserInfo(code));
        assertThat(ex.getMessage()).contains("No response body");
    }

    @Test
    void fetchUserInfo_invalidCode_throwsException() {
        String code = "99999";
        when(restTemplate.exchange(
                eq("http://localhost:3001/api/student/preview/" + code),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(OpacUserInfo.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

        SglException ex = assertThrows(SglException.class, () -> opacService.fetchUserInfo(code));
        assertThat(ex.getMessage()).contains("Invalid code " + code);
    }

    @Test
    void fetchUserInfo_genericError_throwsException() {
        String code = "50000";
        when(restTemplate.exchange(
                eq("http://localhost:3001/api/student/preview/" + code),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(OpacUserInfo.class)
        )).thenThrow(new RuntimeException("Connection refused"));

        SglException ex = assertThrows(SglException.class, () -> opacService.fetchUserInfo(code));
        assertThat(ex.getMessage()).contains("OPAC user info error");
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
