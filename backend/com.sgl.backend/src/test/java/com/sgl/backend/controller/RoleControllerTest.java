package com.sgl.backend.controller;

import com.sgl.backend.entity.Role;
import com.sgl.backend.exception.GlobalExceptionHandler;
import com.sgl.backend.exception.SglException;
import com.sgl.backend.security.JwtAuthenticationFilter;
import com.sgl.backend.security.JwtService;
import com.sgl.backend.service.AuthService;
import com.sgl.backend.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({RoleController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false) 
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleService roleService; 

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter; 

    @MockitoBean
    private AuthService authService;

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createRole_success() throws Exception {
        Role role = Role.builder().id(1L).name("NEW_ROLE").build();
        when(roleService.createRole("NEW_ROLE")).thenReturn(role);

        mockMvc.perform(post("/api/roles")
            .contentType(MediaType.TEXT_PLAIN)
            .content("NEW_ROLE"))               
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("NEW_ROLE"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createRole_alreadyExists_returns400() throws Exception {
        when(roleService.createRole("NEW_ROLE"))
                .thenThrow(new SglException("Role already exists: NEW_ROLE"));

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("NEW_ROLE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Role already exists: NEW_ROLE"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getAllRoles_success() throws Exception {
        List<Role> roles = List.of(Role.builder().id(1L).name("ADMIN").build());
        when(roleService.getAllRoles()).thenReturn(roles);

        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("ADMIN"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void updateRole_success() throws Exception {
        Role role = Role.builder().id(1L).name("UPDATED_ROLE").build();
        when(roleService.updateRole(1L, "UPDATED_ROLE")).thenReturn(role);

        mockMvc.perform(put("/api/roles/1")
            .contentType(MediaType.TEXT_PLAIN)
            .content("UPDATED_ROLE"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("UPDATED_ROLE"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void deleteRole_success() throws Exception {
        doNothing().when(roleService).deleteRole(1L);

        mockMvc.perform(delete("/api/roles/1"))
                .andExpect(status().isNoContent());
    }
}
