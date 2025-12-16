package com.sgl.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sgl.backend.entity.Role;
import com.sgl.backend.entity.User;
import com.sgl.backend.exception.SglException;
import com.sgl.backend.repository.RoleRepository;
import com.sgl.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;   

    public User updateUserRole(String userCode, String roleName) {
        User user = userRepository.findById(userCode)
                .orElseThrow(() -> new SglException("User not found: " + userCode));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new SglException("Role not found: " + roleName));
        user.setRole(role);
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
