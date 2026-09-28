package com.example.sms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StudentRepository {

    public void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS students(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id TEXT NOT NULL UNIQUE,
                name TEXT NOT NULL,
                age INTEGER NOT NULL,
                email TEXT,
                fee_balance REAL NOT NULL DEFAULT 0)
                """;
        try (Connection conn = Db.get();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Student save(Student student) {
        String insertSql = "INSERT INTO students(student_id, name, age, email, fee_balance) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = Db.get()) {

            // Step 1: insert with a temporary student_id
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, "PENDING");
                stmt.setString(2, student.getName());
                stmt.setInt(3, student.getAge());
                stmt.setString(4, student.getEmail());
                stmt.setDouble(5, student.getFeeBalance());
                stmt.executeUpdate();
            }

            // Step 2: ask SQLite which row number it just created
            int newId;
            try (Statement idStmt = conn.createStatement();
                 ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid()")) {
                rs.next();
                newId = rs.getInt(1);
            }

            // Step 3: build the formatted ID, e.g. 1 -> "STU00001"
            String studentId = String.format("STU%05d", newId);

            // Step 4: write the real ID back onto the same row
            try (PreparedStatement update = conn.prepareStatement(
                    "UPDATE students SET student_id = ? WHERE id = ?")) {
                update.setString(1, studentId);
                update.setInt(2, newId);
                update.executeUpdate();
            }

            student.setId(newId);
            student.setStudentId(studentId);
            return student;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Student findByStudentId(String studentId) {
        String sql = "SELECT id, student_id, name, age, email, fee_balance FROM students WHERE student_id = ?";
        try (Connection conn = Db.get();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null; // no student with that ID
    }

    public List<Student> findAll() {
        String sql = "SELECT id, student_id, name, age, email, fee_balance FROM students ORDER BY id";
        List<Student> students = new ArrayList<>();
        try (Connection conn = Db.get();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                students.add(mapRow(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return students;
    }

    public void updateFeeBalance(int id, double newBalance) {
        String sql = "UPDATE students SET fee_balance = ? WHERE id = ?";
        try (Connection conn = Db.get();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, newBalance);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Turns the current database row into a Student object
    private Student mapRow(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("id"),
                rs.getString("student_id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getString("email"),
                rs.getDouble("fee_balance"));
    }
}