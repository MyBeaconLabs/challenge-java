package com.challenge;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/** Base class for tests that need the full application and a real Postgres. */
@SpringBootTest
@Import(IntegrationTest.Containers.class)
@TestPropertySource(properties = "spring.flyway.locations=classpath:db/migration")
public abstract class IntegrationTest {

    @TestConfiguration(proxyBeanMethods = false)
    static class Containers {

        @Bean
        @ServiceConnection
        PostgreSQLContainer<?> postgres() {
            return new PostgreSQLContainer<>("postgres:15-alpine");
        }
    }
}
