package com.example.sms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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

    public List<Course> findCoursesForStudent(int studentId) {
        String sql = """
                SELECT c.id, c.code, c.title, c.fee
                FROM courses c
                JOIN enrollments e ON e.course_id = c.id
                WHERE e.student_id = ?
                ORDER BY c.id
                """;
        List<Course> courses = new ArrayList<>();
        try (Connection conn = Db.get();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Course c = new Course(
                            rs.getString("code"),
                            rs.getString("title"),
                            rs.getDouble("fee"));
                    c.setId(rs.getInt("id"));
                    courses.add(c);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
    }
        return courses;
    }
}