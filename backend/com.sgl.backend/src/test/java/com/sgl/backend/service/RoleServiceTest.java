package com.sgl.backend.service;

import com.sgl.backend.entity.Role;
import com.sgl.backend.exception.SglException;
import com.sgl.backend.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoleServiceTest {
    
    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    @Test
    void createRole_success() {
        when(roleRepository.findByName("NEW_ROLE")).thenReturn(Optional.empty());
        Role savedRole = Role.builder().id(1L).name("NEW_ROLE").build();
        when(roleRepository.save(any(Role.class))).thenReturn(savedRole);

        Role role = roleService.createRole("NEW_ROLE");

        assertThat(role.getName()).isEqualTo("NEW_ROLE");
    }

    @Test
    void createRole_alreadyExists_throwsException() {
        when(roleRepository.findByName("NEW_ROLE")).thenReturn(Optional.of(Role.builder().build()));

        assertThrows(SglException.class, () -> roleService.createRole("NEW_ROLE"),
                "Role already exists: NEW_ROLE");
    }

    @Test
    void getAllRoles_success() {
        List<Role> roles = List.of(Role.builder().id(1L).name("ADMIN").build());
        when(roleRepository.findAll()).thenReturn(roles);

        List<Role> result = roleService.getAllRoles();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("ADMIN");
    }

    @Test
    void updateRole_success() {
        Role role = Role.builder().id(1L).name("OLD_NAME").build();
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.findByName("NEW_NAME")).thenReturn(Optional.empty());
        when(roleRepository.save(role)).thenReturn(role);

        Role updatedRole = roleService.updateRole(1L, "NEW_NAME");

        assertThat(updatedRole.getName()).isEqualTo("NEW_NAME");
    }

    @Test
    void updateRole_roleNotFound_throwsException() {
        when(roleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(SglException.class, () -> roleService.updateRole(1L, "NEW_NAME"),
                "Role not found: 1");
    }

    @Test
    void updateRole_adminRole_throwsException() {
        Role role = Role.builder().id(1L).name("ADMIN").build();
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        SglException ex = assertThrows(SglException.class, () -> roleService.updateRole(1L, "SUPER_ADMIN"));
        assertThat(ex.getMessage()).isEqualTo("Cannot update default role: ADMIN");
    }

    @Test
    void updateRole_nameAlreadyExists_throwsException() {
        Role role = Role.builder().id(1L).name("USER").build();
        Role existing = Role.builder().id(2L).name("NEW_NAME").build();

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.findByName("NEW_NAME")).thenReturn(Optional.of(existing));

        SglException ex = assertThrows(SglException.class, () -> roleService.updateRole(1L, "NEW_NAME"));
        assertThat(ex.getMessage()).isEqualTo("Role name already exists: NEW_NAME");
    }


    @Test
    void deleteRole_success() {
        Role role = Role.builder().id(1L).name("CUSTOM_ROLE").build();
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        roleService.deleteRole(1L);

        verify(roleRepository).delete(role);
    }

    @Test
    void deleteRole_defaultRole_throwsException() {
        Role role = Role.builder().id(1L).name("ADMIN").build();
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        assertThrows(SglException.class, () -> roleService.deleteRole(1L),
                "Cannot delete default role: ADMIN");
    }
}
