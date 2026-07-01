package com.mall.mvp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Stage 0 smoke test: boots the full Spring context against the in-memory H2
 * database and runs Flyway migrations. If this passes, the skeleton wiring
 * (Spring Boot + datasource + Flyway) is sound. This is the gate the CI enforces.
 */
@SpringBootTest
@ActiveProfiles("test")
class MallMvpApplicationTests {

    @Test
    void contextLoads() {
        // Intentionally empty: success = context started and migrations applied.
    }
}
