package com.sgl.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new JwtAuthenticationFilter(jwtService);
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldNotAuthenticate_WhenNoAuthorizationHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticate_WhenAuthorizationHeaderInvalid() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("InvalidToken");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldAuthenticate_WhenValidBearerToken() throws ServletException, IOException {
        String token = "validToken";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractCode(token)).thenReturn("USER123");
        when(jwtService.extractRole(token)).thenReturn("ADMIN");

        filter.doFilterInternal(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("USER123", authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN")));

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticate_WhenTokenInvalid() throws ServletException, IOException {
        String token = "invalidToken";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isTokenValid(token)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
