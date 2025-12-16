package com.sgl.backend.controller;

import com.sgl.backend.entity.Role;
import com.sgl.backend.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "Endpoints for managing roles")
public class RoleController {
    
    private final RoleService roleService;

    @PostMapping
    @Operation(summary = "Create a new role", description = "Creates a new role. Restricted to ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role created successfully"),
            @ApiResponse(responseCode = "400", description = "Role already exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Requires ADMIN role"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Role> createRole(@RequestBody String roleName) {
        Role role = roleService.createRole(roleName);
        return ResponseEntity.ok(role);
    }

    @GetMapping
    @Operation(summary = "Get all roles", description = "Retrieves all roles. Restricted to ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of roles retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Requires ADMIN role"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @PutMapping("/{roleId}")
    @Operation(summary = "Update a role", description = "Updates a role's name. Restricted to ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role updated successfully"),
            @ApiResponse(responseCode = "400", description = "Role not found or name already exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Requires ADMIN role"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Role> updateRole(@PathVariable Long roleId, @RequestBody String roleName) {
        Role role = roleService.updateRole(roleId, roleName);
        return ResponseEntity.ok(role);
    }

    @DeleteMapping("/{roleId}")
    @Operation(summary = "Delete a role", description = "Deletes a role. Cannot delete default roles. Restricted to ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Role deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Role not found or cannot delete default role"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Requires ADMIN role"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return ResponseEntity.noContent().build();
    }
}
