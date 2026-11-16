package com.example.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StudentRepositoryTest {

    private final StudentRepository repo = new StudentRepository();

    @BeforeEach
    void useFreshDatabase(@TempDir Path tempDir) {
        System.setProperty("sms.db.url", "jdbc:sqlite:" + tempDir.resolve("test.db"));
        repo.createTable();
    }

    @AfterEach
    void restoreRealDatabase() {
        System.clearProperty("sms.db.url");
    }

    @Test
    void saveGeneratesFormattedStudentId() {
        Student saved = repo.save(new Student("John", 20, "john@example.com"));

        assertEquals(1, saved.getId());
        assertEquals("STU00001", saved.getStudentId());
    }

    @Test
    void secondStudentGetsTheNextId() {
        repo.save(new Student("John", 20, "john@example.com"));

        Student second = repo.save(new Student("Jane", 19, "jane@example.com"));

        assertEquals("STU00002", second.getStudentId());
    }

    @Test
    void findByStudentIdReturnsTheSavedStudent() {
        Student saved = repo.save(new Student("Jane", 19, "jane@example.com"));

        Student found = repo.findByStudentId(saved.getStudentId());

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
        assertEquals("Jane", found.getName());
        assertEquals(19, found.getAge());
    }

    @Test
    void findByStudentIdReturnsNullWhenMissing() {
        assertNull(repo.findByStudentId("STU99999"));
    }

    @Test
    void updatedFeeBalanceSurvivesReloading() {
        Student saved = repo.save(new Student("John", 20, "john@example.com"));

        repo.updateFeeBalance(saved.getId(), 250.00);

        Student reloaded = repo.findByStudentId(saved.getStudentId());
        assertEquals(250.00, reloaded.getFeeBalance(), 0.001);
    }
}