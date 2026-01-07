package ru.itmo.is;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SpringAppIntegrationTest extends AbstractIntegrationTest {

    @Test
    @Transactional
    void contextLoads() {
        assertNotNull(applicationContext, "Application context should be loaded");
    }

    @Test
    @Transactional
    void springAppBeanShouldExist() {
        SpringApp springApp = applicationContext.getBean(SpringApp.class);
        assertNotNull(springApp, "SpringApp bean should be present in context");
    }

    @Test
    @Transactional
    void testDataBuilderShouldBeAvailable() {
        assertNotNull(testDataBuilder, "TestDataBuilder should be available");
    }

    @Test
    @Transactional
    void jwtHelperShouldBeAvailable() {
        assertNotNull(jwtHelper, "JwtTestHelper should be available");
    }
}

