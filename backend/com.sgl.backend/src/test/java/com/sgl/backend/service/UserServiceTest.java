package com.sgl.backend.service;

import com.sgl.backend.entity.Role;
import com.sgl.backend.entity.User;
import com.sgl.backend.exception.SglException;
import com.sgl.backend.repository.RoleRepository;
import com.sgl.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void updateUserRole_success() {
        Role role = Role.builder().id(1L).name("MONITOR").build();
        User user = User.builder().code("12345").role(Role.builder().name("ESTUDIANTE").build()).build();
        when(userRepository.findById("12345")).thenReturn(Optional.of(user));
        when(roleRepository.findByName("MONITOR")).thenReturn(Optional.of(role));
        when(userRepository.save(user)).thenReturn(user);

        User updatedUser = userService.updateUserRole("12345", "MONITOR");

        assertThat(updatedUser.getRole().getName()).isEqualTo("MONITOR");
    }

    @Test
    void updateUserRole_userNotFound_throwsException() {
        when(userRepository.findById("12345")).thenReturn(Optional.empty());

        assertThrows(SglException.class, () -> userService.updateUserRole("12345", "MONITOR"),
                "User not found: 12345");
    }

    @Test
    void updateUserRole_roleNotFound_throwsException() {
        User user = User.builder().code("12345").role(Role.builder().name("ESTUDIANTE").build()).build();
        when(userRepository.findById("12345")).thenReturn(Optional.of(user));
        when(roleRepository.findByName("MONITOR")).thenReturn(Optional.empty());

        assertThrows(SglException.class, () -> userService.updateUserRole("12345", "MONITOR"),
                "Role not found: MONITOR");
    }
}
