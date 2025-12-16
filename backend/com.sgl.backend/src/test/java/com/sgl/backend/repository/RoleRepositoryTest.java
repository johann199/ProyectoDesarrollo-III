package com.sgl.backend.repository;

import com.sgl.backend.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void findByName_existingRole_returnsRole() {
        Role role = new Role();
        role.setName("MONITOR");
        roleRepository.save(role);

        Optional<Role> found = roleRepository.findByName("MONITOR");
        assertTrue(found.isPresent());
        assertEquals("MONITOR", found.get().getName());
    }

    @Test
    void findByName_nonExistingRole_returnsEmpty() {
        Optional<Role> found = roleRepository.findByName("NOT_EXISTING");
        assertTrue(found.isEmpty());
    }
}
