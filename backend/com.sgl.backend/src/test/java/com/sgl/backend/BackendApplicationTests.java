package com.sgl.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(properties = "spring.profiles.active=test")
class BackendApplicationTests {

    @Test
    void contextLoads() { 
        // Test to ensure the Spring application context loads successfully
    }
}
