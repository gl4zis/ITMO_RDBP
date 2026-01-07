package ru.itmo.is.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Component
public class DatabaseCleaner {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void cleanAll() {
        entityManager.createNativeQuery("SET session_replication_role = 'replica'").executeUpdate();

        truncateTable("bid_file");
        truncateTable("bid");
        truncateTable("event");
        truncateTable("resident");
        truncateTable("room");
        truncateTable("dormitory");
        truncateTable("university_dormitory");
        truncateTable("university");
        truncateTable("usr");

        entityManager.createNativeQuery("SET session_replication_role = 'origin'").executeUpdate();
        assertEquals(0, getAllTableNames().size());
    }

    @Transactional
    public void truncateTable(String tableName) {
        entityManager
                .createNativeQuery("TRUNCATE TABLE " + tableName + " RESTART IDENTITY CASCADE")
                .executeUpdate();
    }

    @Transactional
    public void cleanTables(String... tableNames) {
        for (String tableName : tableNames) {
            truncateTable(tableName);
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getAllTableNames() {
        return entityManager.createNativeQuery(
                "SELECT tablename FROM pg_tables WHERE schemaname = 'public'"
        ).getResultList();
    }
}

