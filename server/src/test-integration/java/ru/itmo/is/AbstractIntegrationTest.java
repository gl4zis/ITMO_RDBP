package ru.itmo.is;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.itmo.is.util.JwtTestHelper;
import ru.itmo.is.util.TestDataBuilder;

/**
 * Base class for all integration tests.
 * Provides PostgreSQL container setup and common test utilities.
 * 
 * Note: Tests should use @Transactional at method level if they need automatic rollback.
 * For tests that need to verify committed data, omit @Transactional and clean up manually.
 */
@SpringBootTest
@ActiveProfiles("tests")
@Testcontainers
@TestPropertySource(properties = {
    "spring.datasource.hikari.maximum-pool-size=20",
    "spring.datasource.hikari.minimum-idle=5",
    "spring.datasource.hikari.connection-timeout=3000",
    "spring.datasource.hikari.idle-timeout=30000",
    "spring.datasource.hikari.max-lifetime=180000",
    "spring.datasource.hikari.leak-detection-threshold=6000",
    "spring.datasource.hikari.connection-test-query=SELECT 1",
    "spring.jpa.properties.hibernate.connection.provider_disables_autocommit=false"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true)
            .withStartupTimeout(java.time.Duration.ofSeconds(60))
            .withConnectTimeoutSeconds(30);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    protected ApplicationContext applicationContext;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    protected TestDataBuilder testDataBuilder;

    @Autowired
    protected JwtTestHelper jwtHelper;

    @BeforeEach
    void setUp() {
        if (entityManager != null && entityManager.isOpen()) {
            entityManager.clear();
        }
    }

    @AfterEach
    void tearDown() {
        // Clear entity manager
        if (entityManager != null && entityManager.isOpen()) {
            try {
                entityManager.clear();
            } catch (Exception e) {
                // Ignore if already closed
            }
        }
    }

    /**
     * Flush and clear the persistence context.
     * Useful when you need to ensure database state is synchronized.
     */
    protected void flushAndClear() {
        if (entityManager != null && entityManager.isOpen()) {
            try {
                entityManager.flush();
                entityManager.clear();
            } catch (Exception e) {
                // Ignore if transaction is already closed
            }
        }
    }

}

