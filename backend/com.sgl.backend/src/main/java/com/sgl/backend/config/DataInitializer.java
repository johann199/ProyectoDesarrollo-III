package com.sgl.backend.config;


import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.sgl.backend.entity.Equipment;
import com.sgl.backend.entity.Laboratory;
import com.sgl.backend.entity.Role;
import com.sgl.backend.entity.User;
import com.sgl.backend.repository.EquipmentRepository;
import com.sgl.backend.repository.LaboratoryRepository;
import com.sgl.backend.repository.RoleRepository;
import com.sgl.backend.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final LaboratoryRepository labRepo;
    private final EquipmentRepository equipmentRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createRoleIfNotExists("ADMIN");
        createRoleIfNotExists("MONITOR");
        createRoleIfNotExists("DOCENTE");
        createRoleIfNotExists("ESTUDIANTE");

        if (!userRepository.existsById("admin_code")) {
            Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
            String hashedPassword = passwordEncoder.encode("admin_password");
            User admin = User.builder()
                    .code("admin_code")
                    .name("Admin User")
                    .email("admin@correounivalle.edu.co")
                    .document("123456789")
                    .password(hashedPassword)
                    .role(adminRole)
                    .build();
            userRepository.save(admin);
        }
    }

    private void createRoleIfNotExists(String name) {
        if (roleRepository.findByName(name).isEmpty()) {
            Role role = Role.builder().name(name).build();
            roleRepository.save(role);
        }
    }

    @PostConstruct
    public void initData() {
        if (labRepo.count() == 0) {
            labRepo.save(Laboratory.builder()
                    .name("Laboratorio Principal")
                    .capacity(18)
                    .active(true)
                    .build());
        }

        if (equipmentRepo.count() == 0) {
            equipmentRepo.save(
                Equipment.builder()
                    .barcode("EQ-001")
                    .name("Destornillador")
                    .totalUnits(3)
                    .availableUnits(3)
                    .status(Equipment.EquipmentStatus.AVAILABLE)
                    .build()
            );

            equipmentRepo.save(
                Equipment.builder()
                    .barcode("EQ-002")
                    .name("Soldador Eléctrico")
                    .totalUnits(2)
                    .availableUnits(2)
                    .status(Equipment.EquipmentStatus.AVAILABLE)
                    .build()
            );
        }
    }
}
