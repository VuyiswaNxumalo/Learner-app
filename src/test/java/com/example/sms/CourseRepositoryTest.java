package com.example.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CourseRepositoryTest {

    private final CourseRepository repo = new CourseRepository();

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
    void saveAssignsAnIdToTheCourse() {
        Course saved = repo.save(new Course("CS101", "Intro to CS", 150.00));

        assertNotNull(saved);
        assertTrue(saved.getId() > 0);
    }

    @Test
    void saveReturnsNullForDuplicateCode() {
        repo.save(new Course("CS101", "Intro to CS", 150.00));

        Course duplicate = repo.save(new Course("CS101", "Something else", 99.00));

        assertNull(duplicate);
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void findByIdReturnsNullWhenCourseDoesNotExist() {
        assertNull(repo.findById(999));
    }
}