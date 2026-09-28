package com.example.sms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CourseRepository {

    public void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS courses(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                code TEXT NOT NULL UNIQUE,
                title TEXT NOT NULL,
                fee REAL NOT NULL)
                """;
        try (Connection conn = Db.get();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Course save(Course course) {
        String sql = "INSERT INTO courses(code, title, fee) VALUES(?, ?, ?)";
        try (Connection conn = Db.get();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, course.getCode());
            stmt.setString(2, course.getTitle());
            stmt.setDouble(3, course.getFee());
            stmt.executeUpdate();

            // ask SQLite which row number it just created
            try (Statement idStmt = conn.createStatement();
                 ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    course.setId(rs.getInt(1));
                }
            }
            return course;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Course> findAll() {
        String sql = "SELECT id, code, title, fee FROM courses ORDER BY id";
        List<Course> courses = new ArrayList<>();
        try (Connection conn = Db.get();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Course c = new Course(
                        rs.getString("code"),
                        rs.getString("title"),
                        rs.getDouble("fee"));
                c.setId(rs.getInt("id"));
                courses.add(c);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return courses;
    }

    public Course findById(int id) {
        String sql = "SELECT id, code, title, fee FROM courses WHERE id = ?";
        try (Connection conn = Db.get();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Course c = new Course(
                            rs.getString("code"),
                            rs.getString("title"),
                            rs.getDouble("fee"));
                    c.setId(rs.getInt("id"));
                    return c;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null; // no course with that id
    }
}