package com.example.sms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

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

            //  insert with a temporary student_id
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, "PENDING");
                stmt.setString(2, student.getName());
                stmt.setInt(3, student.getAge());
                stmt.setString(4, student.getEmail());
                stmt.setDouble(5, student.getFeeBalance());
                stmt.executeUpdate();
            }

            //  ask SQLite which row number it just created
            int newId;
            try (Statement idStmt = conn.createStatement();
                 ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid()")) {
                rs.next();
                newId = rs.getInt(1);
            }

            // build the formatted ID, e.g. 1 -> "STU00001"
            String studentId = String.format("STU%05d", newId);

            //  write the real ID back onto the same row
            try (PreparedStatement update = conn.prepareStatement(
                    "UPDATE students SET student_id = ? WHERE id = ?")) {
                update.setString(1, studentId);
                update.setInt(2, newId);
                update.executeUpdate();
            }

            //  fill the ID into the Student object the caller gave us
            student.setId(newId);
            student.setStudentId(studentId);
            return student;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}