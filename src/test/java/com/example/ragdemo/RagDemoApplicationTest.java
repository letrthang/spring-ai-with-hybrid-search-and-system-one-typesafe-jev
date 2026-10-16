package com.example.ragdemo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/** The whole app starts (Vaadin, JPA, H2). Tests use an in-memory DB so they never touch ./data. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
class RagDemoApplicationTest {

    @Test
    void contextLoads() {
    }
}
