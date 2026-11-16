package com.example.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EnrollmentServiceTest {

    private final StudentRepository studentRepo = new StudentRepository();
    private final CourseRepository courseRepo = new CourseRepository();
    private final EnrollmentRepository enrollmentRepo = new EnrollmentRepository();
    private final EnrollmentService service = new EnrollmentService();

    private Student john;
    private Course cs101;
    private Course math201;

    @BeforeEach
    void useFreshDatabase(@TempDir Path tempDir) {
        System.setProperty("sms.db.url", "jdbc:sqlite:" + tempDir.resolve("test.db"));
        studentRepo.createTable();
        courseRepo.createTable();
        enrollmentRepo.createTable();

        john = studentRepo.save(new Student("John", 20, "john@example.com"));
        cs101 = courseRepo.save(new Course("CS101", "Intro to CS", 150.00));
        math201 = courseRepo.save(new Course("MATH201", "Calculus II", 120.00));
    }

    @AfterEach
    void restoreRealDatabase() {
        System.clearProperty("sms.db.url");
    }

    // helper: always read the balance back from the database
    private double storedBalance() {
        return studentRepo.findByStudentId(john.getStudentId()).getFeeBalance();
    }

    @Test
    void enrollingChargesTheCourseFee() {
        service.enroll(john.getStudentId(), cs101.getId());

        assertEquals(150.00, storedBalance(), 0.001);
    }

    @Test
    void enrollingInTwoCoursesAddsBothFees() {
        service.enroll(john.getStudentId(), cs101.getId());
        service.enroll(john.getStudentId(), math201.getId());

        assertEquals(270.00, storedBalance(), 0.001);
    }

    @Test
    void enrollmentShowsUpInTheStudentsCourseList() {
        service.enroll(john.getStudentId(), cs101.getId());

        List<Course> courses = enrollmentRepo.findCoursesForStudent(john.getId());

        assertEquals(1, courses.size());
        assertEquals("CS101", courses.get(0).getCode());
    }

    @Test
    void duplicateEnrollmentIsRejectedAndNotChargedTwice() {
        service.enroll(john.getStudentId(), cs101.getId());

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.enroll(john.getStudentId(), cs101.getId()));

        assertTrue(e.getMessage().contains("already enrolled"));
        assertEquals(150.00, storedBalance(), 0.001);   // still one fee, not two
        assertEquals(1, enrollmentRepo.findCoursesForStudent(john.getId()).size());
    }

    @Test
    void unknownStudentIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.enroll("STU99999", cs101.getId()));

        assertTrue(e.getMessage().contains("STU99999"));
    }

    @Test
    void unknownCourseIsRejectedAndNothingIsCharged() {
        assertThrows(IllegalArgumentException.class,
                () -> service.enroll(john.getStudentId(), 999));

        assertEquals(0.0, storedBalance(), 0.001);
    }
}