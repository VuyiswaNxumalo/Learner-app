package com.example.sms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class EnrollmentRepository {

    public void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS enrollments(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id INTEGER NOT NULL,
                course_id INTEGER NOT NULL,
                UNIQUE(student_id, course_id))
                """;
        try (Connection conn = Db.get();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tries to enroll a student in a course.
     * Returns true if it worked, false if they were already enrolled.
     */
    public boolean enroll(int studentId, int courseId) {
        String sql = "INSERT INTO enrollments(student_id, course_id) VALUES(?, ?)";
        try (Connection conn = Db.get();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);
            stmt.executeUpdate();
            return true; 

        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                return false; 
            }
            throw new RuntimeException(e);
        }
    }
}