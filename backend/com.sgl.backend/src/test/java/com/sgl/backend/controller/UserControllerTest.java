package com.sgl.backend.controller;

import com.sgl.backend.entity.Role;
import com.sgl.backend.entity.User;
import com.sgl.backend.exception.SglException;
import com.sgl.backend.security.JwtAuthenticationFilter;
import com.sgl.backend.security.JwtService;
import com.sgl.backend.service.AuthService;
import com.sgl.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false) 
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter; 

    @Test
    @WithMockUser(authorities = "ADMIN")
    void updateUserRole_success() throws Exception {
        Role role = Role.builder().id(1L).name("MONITOR").build();
        User user = User.builder().code("12345").role(role).build();

        when(userService.updateUserRole("12345", "MONITOR")).thenReturn(user);

        mockMvc.perform(put("/api/users/12345/role")
                .param("roleName", "MONITOR")) 
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("12345"))
        .andExpect(jsonPath("$.role.name").value("MONITOR"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void updateUserRole_userNotFound_returns400() throws Exception {
        when(userService.updateUserRole(anyString(), anyString()))
                .thenThrow(new SglException("User not found: 12345"));

        mockMvc.perform(put("/api/users/12345/role")
                        .param("roleName", "MONITOR")) 
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("User not found: 12345"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getAllUsers_success() throws Exception {
        Role role = Role.builder().id(1L).name("ESTUDIANTE").build();
        User user = User.builder().code("12345").role(role).build();
        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("12345"))
                .andExpect(jsonPath("$[0].role.name").value("ESTUDIANTE"));
    }
}
