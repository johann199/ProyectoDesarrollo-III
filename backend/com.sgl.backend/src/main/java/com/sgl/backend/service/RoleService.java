package com.sgl.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sgl.backend.entity.Role;
import com.sgl.backend.exception.SglException;
import com.sgl.backend.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Role createRole(String roleName) {
        if (roleRepository.findByName(roleName).isPresent()) {
            throw new SglException("Role already exists: " + roleName);
        }
        Role role = Role.builder()
                .name(roleName)
                .build();
        return roleRepository.save(role);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role updateRole(Long roleId, String roleName) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new SglException("Role not found: " + roleId));
        if("ADMIN".equals(role.getName())) {
            throw new SglException("Cannot update default role: ADMIN");
        }
        if (roleRepository.findByName(roleName).isPresent() && !role.getName().equals(roleName)) {
            throw new SglException("Role name already exists: " + roleName);
        }
        role.setName(roleName);
        return roleRepository.save(role);
    }

    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new SglException("Role not found: " + roleId));
        if (role.getName().equals("ADMIN") || role.getName().equals("ESTUDIANTE") || role.getName().equals("DOCENTE")) {
            throw new SglException("Cannot delete default role: " + role.getName());
        }
        roleRepository.delete(role);
    }
}
